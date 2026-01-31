package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.order.OrderRequest;
import com.hygor.makeup_api.dto.order.OrderResponse;
import com.hygor.makeup_api.dto.order.OrderItemResponse;
import com.hygor.makeup_api.dto.shipping.ShippingOptionResponse;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService extends BaseService<Order, OrderRepository> {

    // Substituído ProductRepository por ProductVariantRepository para focar em SKUs
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ShippingService shippingService;
    private final CartRepository cartRepository;

    public OrderService(OrderRepository repository,
            ProductVariantRepository variantRepository,
            UserRepository userRepository,
            AddressRepository addressRepository,
            ShippingService shippingService,
            CartRepository cartRepository) {
        super(repository);
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.shippingService = shippingService;
        this.cartRepository = cartRepository;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));

        // 1. Busca o Endereço e calcula o Frete Real 🚚
        Address address = addressRepository.findById(request.getAddressId())
                .filter(a -> a.getUser().getEmail().equals(email))
                .orElseThrow(() -> new RuntimeException("Endereço de entrega inválido."));

        ShippingOptionResponse shipping = shippingService.calculateBestOption(address.getZipCode());

        // 2. Inicia a Entidade Pedido
        Order order = Order.builder()
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.WAITING_PAYMENT)
                .customer(customer)
                .items(new ArrayList<>())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        // 3. Processa Itens e Valida Stock por VARIANTE (Cor/Tom) 🛡️
        for (var itemRequest : request.getItems()) {
            ProductVariant variant = variantRepository.findById(itemRequest.getVariantId())
                    .orElseThrow(
                            () -> new RuntimeException("Variação não encontrada ID: " + itemRequest.getVariantId()));

            if (variant.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Estoque insuficiente para: " + variant.getProduct().getName() + " ("
                        + variant.getName() + ")");
            }

            // Baixa estoque da cor específica
            variant.setStockQuantity(variant.getStockQuantity() - itemRequest.getQuantity());
            variantRepository.save(variant);

            OrderItem orderItem = OrderItem.builder()
                    .variant(variant)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(variant.getPrice()) // Preço da variante (pode ser diferente do produto mestre)
                    .order(order)
                    .build();

            order.getItems().add(orderItem);
            subtotal = subtotal.add(orderItem.getUnitPrice().multiply(new BigDecimal(orderItem.getQuantity())));
        }

        order.setSubtotal(subtotal);

        // 4. Aplica Cupão do Carrinho 🏷️ ✨
        BigDecimal discount = BigDecimal.ZERO;
        Cart cart = cartRepository.findByUserEmail(email).orElse(null);
        if (cart != null && cart.getCoupon() != null && cart.getCoupon().isValid()) {
            discount = subtotal.multiply(BigDecimal.valueOf(cart.getCoupon().getDiscountPercentage() / 100));
            order.setDiscountAmount(discount);
            cart.getCoupon().setUsedCount(cart.getCoupon().getUsedCount() + 1);
        } else {
            order.setDiscountAmount(BigDecimal.ZERO);
        }

        // 5. REGRA DE FRETE GRÁTIS 🎁 ✨
        BigDecimal finalShippingFee = shipping.getPrice();
        BigDecimal freeShippingThreshold = new BigDecimal("200.00");

        if (subtotal.compareTo(freeShippingThreshold) >= 0) {
            log.info("Parabéns! Pedido qualificado para Frete Grátis.");
            finalShippingFee = BigDecimal.ZERO;
            order.setShippingMethod(shipping.getName() + " (Grátis)");
        } else {
            order.setShippingMethod(shipping.getName());
        }

        order.setShippingFee(finalShippingFee);

        // 6. Cálculo Final
        order.setTotalAmount(subtotal.subtract(discount).add(finalShippingFee));

        Order savedOrder = repository.save(order);

        // 7. Limpa o carrinho após o sucesso 🧹
        if (cart != null) {
            cart.getItems().clear();
            cart.setCoupon(null);
            cartRepository.save(cart);
        }

        log.info("Pedido {} finalizado. Total: R$ {}", savedOrder.getOrderNumber(), savedOrder.getTotalAmount());
        return toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse cancelOrder(String orderNumber) {
        Order order = findEntityByOrderNumber(orderNumber);

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Não é possível cancelar um pedido já enviado.");
        }

        // Devolve o estoque para as variantes específicas
        for (OrderItem item : order.getItems()) {
            ProductVariant variant = item.getVariant();
            variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
            variantRepository.save(variant);
        }

        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(repository.save(order));
    }

    // Métodos de busca mantidos conforme original, apenas garantindo consistência
    @Transactional(readOnly = true)
    public Order findEntityByOrderNumber(String orderNumber) {
        return repository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + orderNumber));
    }

    @Transactional
    public void saveOrder(Order order) {
        repository.save(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return repository.findByCustomerEmail(email, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getByOrderNumber(String orderNumber) {
        return repository.findByOrderNumber(orderNumber)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + orderNumber));
    }

    @Transactional
    public void updateOrderStatusByPaymentId(String externalId, OrderStatus newStatus) {
        Order order = repository.findByPaymentExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado para o pagamento: " + externalId));
        order.setStatus(newStatus);
        repository.save(order);
    }

    public OrderResponse toResponse(Order order) {
        if (order == null)
            return null;

        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map((OrderItem item) -> {
                    ProductVariant variant = item.getVariant();
                    Product product = variant.getProduct();

                    return OrderItemResponse.builder()
                            .variantId(variant.getId())
                            .productName(product.getName() + " - " + variant.getName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .subtotal(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                            .build();
                })
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .shippingMethod(order.getShippingMethod())
                .trackingCode(order.getTrackingCode())
                .items(itemResponses)
                .build();
    }

    public void processPaymentNotification(Order order, PaymentStatus newPaymentStatus) {
        // Idempotência: Se o status já for o mesmo, não faz nada
        if (order.getPayment().getStatus() == newPaymentStatus) {
            log.info("Pedido {} já processado com o status {}. Ignorando atualização.", order.getOrderNumber(),
                    newPaymentStatus);
            return;
        }

        Payment payment = order.getPayment();
        payment.setStatus(newPaymentStatus);

        switch (newPaymentStatus) {
            case APPROVED:
                log.info("Pagamento aprovado para o pedido {}. Preparando envio.", order.getOrderNumber());
                // Se estava cancelado antes (caso raro de race condition), retira stock
                // novamente?
                // Assumindo fluxo normal: Muda para PROCESSING (Pronto para embalar)
                order.setStatus(OrderStatus.PROCESSING);
                break;

            case CANCELLED:
            case REFUNDED:
                log.info("Pagamento cancelado/rejeitado para o pedido {}. Devolvendo stock.", order.getOrderNumber());
                order.setStatus(OrderStatus.CANCELLED);
                restoreStock(order); // Método auxiliar para devolver stock
                break;

            default:
                // PENDING ou outros status intermediários não alteram o fluxo crítico
                break;
        }

        repository.save(order);
    }

    private void restoreStock(Order order) {
        // Só devolve stock se o pedido ainda não tiver sido devolvido (proteção extra)
        for (OrderItem item : order.getItems()) {
            ProductVariant variant = item.getVariant();
            // Devolve a quantidade ao stock disponível
            variant.setStockQuantity(variant.getStockQuantity() + item.getQuantity());
            variantRepository.save(variant);
            log.debug("Stock restaurado para SKU {}: +{}", variant.getSku(), item.getQuantity());
        }
    }
}