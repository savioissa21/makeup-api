package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.order.OrderRequest;
import com.hygor.makeup_api.dto.order.OrderResponse;
import com.hygor.makeup_api.dto.shipping.ShippingOptionResponse;
import com.hygor.makeup_api.exception.custom.BusinessException;
import com.hygor.makeup_api.exception.custom.ResourceNotFoundException;
import com.hygor.makeup_api.mapper.OrderMapper;
import com.hygor.makeup_api.model.*;
import com.hygor.makeup_api.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
@Slf4j
public class OrderService extends BaseService<Order, OrderRepository> {

    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ShippingService shippingService;
    private final CartRepository cartRepository;
    private final OrderMapper orderMapper;
    private final EmailService emailService; // Novo Serviço de E-mail

    public OrderService(OrderRepository repository,
                        ProductVariantRepository variantRepository,
                        UserRepository userRepository,
                        AddressRepository addressRepository,
                        ShippingService shippingService,
                        CartRepository cartRepository,
                        OrderMapper orderMapper,
                        EmailService emailService) {
        super(repository);
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.shippingService = shippingService;
        this.cartRepository = cartRepository;
        this.orderMapper = orderMapper;
        this.emailService = emailService;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado."));

        // 1. Valida Endereço e Calcula Frete
        Address address = addressRepository.findById(request.getAddressId())
                .filter(a -> a.getUser().getEmail().equals(email))
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado ou não pertence ao usuário."));

        ShippingOptionResponse shipping = shippingService.calculateBestOption(address.getZipCode());

        // 2. Prepara Pedido Inicial
        Order order = Order.builder()
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.WAITING_PAYMENT)
                .customer(customer)
                .items(new ArrayList<>())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        // 3. Processa Itens e Valida Stock (Crítico ⚠️)
        for (var itemRequest : request.getItems()) {
            ProductVariant variant = variantRepository.findById(itemRequest.getVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variação não encontrada ID: " + itemRequest.getVariantId()));

            if (variant.getStockQuantity() < itemRequest.getQuantity()) {
                throw new BusinessException("Stock insuficiente para: " + variant.getProduct().getName() + " - " + variant.getName());
            }

            // Baixa de Stock Atômica
            variant.setStockQuantity(variant.getStockQuantity() - itemRequest.getQuantity());
            variantRepository.save(variant);

            OrderItem orderItem = OrderItem.builder()
                    .variant(variant)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(variant.getPrice())
                    .order(order)
                    .build();

            order.getItems().add(orderItem);
            subtotal = subtotal.add(orderItem.getUnitPrice().multiply(new BigDecimal(orderItem.getQuantity())));
        }

        order.setSubtotal(subtotal);

        // 4. Aplica Cupão do Carrinho
        BigDecimal discount = BigDecimal.ZERO;
        Cart cart = cartRepository.findByUserEmail(email).orElse(null);
        
        if (cart != null && cart.getCoupon() != null && cart.getCoupon().isValid()) {
            discount = subtotal.multiply(BigDecimal.valueOf(cart.getCoupon().getDiscountPercentage() / 100));
            order.setDiscountAmount(discount);
            
            // Incrementa uso do cupão
            Coupon coupon = cart.getCoupon();
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            // couponRepository.save(coupon); // Cascade trata, mas salvar explícito é seguro
        } else {
            order.setDiscountAmount(BigDecimal.ZERO);
        }

        // 5. Frete Grátis
        BigDecimal finalShippingFee = shipping.getPrice();
        BigDecimal freeShippingThreshold = new BigDecimal("200.00");

        if (subtotal.compareTo(freeShippingThreshold) >= 0) {
            finalShippingFee = BigDecimal.ZERO;
            order.setShippingMethod(shipping.getName() + " (Grátis)");
        } else {
            order.setShippingMethod(shipping.getName());
        }

        order.setShippingFee(finalShippingFee);
        order.setTotalAmount(subtotal.subtract(discount).add(finalShippingFee));

        Order savedOrder = repository.save(order);

        // 6. Limpeza do Carrinho
        if (cart != null) {
            cart.getItems().clear();
            cart.setCoupon(null);
            cartRepository.save(cart);
        }

        // 7. NOTIFICAÇÃO POR E-MAIL (Async) 📧
        OrderResponse response = orderMapper.toResponse(savedOrder);
        emailService.sendOrderConfirmation(response);

        log.info("Pedido criado com sucesso: {} | Total: {}", savedOrder.getOrderNumber(), savedOrder.getTotalAmount());
        return response;
    }

    @Transactional
    public OrderResponse cancelOrder(String orderNumber) {
        Order order = findEntityByOrderNumber(orderNumber);

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("Não é possível cancelar um pedido que já foi enviado ou entregue.");
        }

        // Restaura o Stock
        restoreStock(order);

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = repository.save(order);
        
        log.info("Pedido {} cancelado pelo utilizador.", orderNumber);
        
        // Notifica cancelamento (opcional, mas recomendado)
        emailService.sendOrderStatusUpdate(orderMapper.toResponse(saved));
        
        return orderMapper.toResponse(saved);
    }

    /**
     * Atualiza status via Webhook de Pagamento.
     */
    @Transactional
    public void processPaymentNotification(Order order, PaymentStatus newPaymentStatus) {
        if (order.getPayment() == null) {
            log.error("Erro crítico: Pedido {} não tem pagamento associado.", order.getOrderNumber());
            return;
        }

        if (order.getPayment().getStatus() == newPaymentStatus) {
            return; // Idempotência
        }

        order.getPayment().setStatus(newPaymentStatus);

        boolean statusChanged = false;

        switch (newPaymentStatus) {
            case APPROVED:
                order.setStatus(OrderStatus.PROCESSING);
                log.info("Pagamento APROVADO para pedido {}.", order.getOrderNumber());
                statusChanged = true;
                break;
            case CANCELLED:
            case REFUNDED:
            case CHARGED_BACK:
                order.setStatus(OrderStatus.CANCELLED);
                restoreStock(order);
                log.info("Pagamento REJEITADO para pedido {}. Stock restaurado.", order.getOrderNumber());
                statusChanged = true;
                break;
            default:
                break;
        }
        
        Order savedOrder = repository.save(order);

        // NOTIFICAÇÃO POR E-MAIL (Async) 📧
        // Envia apenas se houve mudança relevante de status (Aprovado ou Cancelado)
        if (statusChanged) {
            emailService.sendOrderStatusUpdate(orderMapper.toResponse(savedOrder));
        }
    }

    // --- Métodos Auxiliares ---

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            ProductVariant variant = item.getVariant();
            variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
            variantRepository.save(variant);
            log.debug("Stock restaurado: {} (+{})", variant.getSku(), item.getQuantity());
        }
    }

    @Transactional(readOnly = true)
    public Order findEntityByOrderNumber(String orderNumber) {
        return repository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + orderNumber));
    }

    @Transactional
    public void saveOrder(Order order) {
        repository.save(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return repository.findByCustomerEmail(email, pageable)
                .map(orderMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getByOrderNumber(String orderNumber) {
        return repository.findByOrderNumber(orderNumber)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + orderNumber));
    }
}