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

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ShippingService shippingService;
    private final CartRepository cartRepository;

    public OrderService(OrderRepository repository, 
                        ProductRepository productRepository, 
                        UserRepository userRepository,
                        AddressRepository addressRepository,
                        ShippingService shippingService,
                        CartRepository cartRepository) {
        super(repository);
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.shippingService = shippingService;
        this.cartRepository = cartRepository;
    }

    /**
     * CRIAÇÃO DE PEDIDO: Valida stock, frete e cupons. 💎 ✨
     */
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));

        // 1. Busca o Endereço e calcula o Frete Real via Melhor Envio 🚚
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
                .shippingMethod(shipping.getName()) // Salva o método (Ex: Sedex)
                .shippingFee(shipping.getPrice())   // Salva o valor do frete
                .items(new ArrayList<>())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        // 3. Processa Itens e Valida Stock
        for (var itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado ID: " + itemRequest.getProductId()));

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Estoque insuficiente para: " + product.getName());
            }

            // Baixa de stock imediata 📉
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .order(order)
                    .build();

            order.getItems().add(orderItem);
            subtotal = subtotal.add(orderItem.getUnitPrice().multiply(new BigDecimal(orderItem.getQuantity())));
        }

        order.setSubtotal(subtotal);

        // 4. Aplica Cupão do Carrinho (se existir e for válido) 🏷️ ✨
        BigDecimal discount = BigDecimal.ZERO;
        Cart cart = cartRepository.findByUserEmail(email).orElse(null);
        if (cart != null && cart.getCoupon() != null && cart.getCoupon().isValid()) {
            discount = subtotal.multiply(BigDecimal.valueOf(cart.getCoupon().getDiscountPercentage() / 100));
            order.setDiscountAmount(discount);
            // Incrementa o uso do cupão no sistema
            cart.getCoupon().setUsedCount(cart.getCoupon().getUsedCount() + 1);
        } else {
            order.setDiscountAmount(BigDecimal.ZERO);
        }

        // 5. Cálculo Final: (Subtotal - Desconto) + Frete 🛡️ 💎
        order.setTotalAmount(subtotal.subtract(discount).add(shipping.getPrice()));

        Order savedOrder = repository.save(order);
        
        // 6. Limpa o carrinho para a próxima compra 🧹
        if (cart != null) {
            cart.getItems().clear();
            cart.setCoupon(null);
            cartRepository.save(cart);
        }

        log.info("Pedido {} finalizado. Total: R$ {}", savedOrder.getOrderNumber(), savedOrder.getTotalAmount());
        return toResponse(savedOrder);
    }

    /**
     * NOVO: Busca a entidade real para processamento interno (Pagamentos/Webhook). 🔐 ✨
     */
    @Transactional(readOnly = true)
    public Order findEntityByOrderNumber(String orderNumber) {
        return repository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + orderNumber));
    }

    /**
     * NOVO: Permite ao PaymentController salvar o vínculo do pagamento no pedido. 🔗
     */
    @Transactional
    public void saveOrder(Order order) {
        repository.save(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return repository.findByCustomerEmail(email, pageable).map(this::toResponse); //
    }

    @Transactional(readOnly = true)
    public OrderResponse getByOrderNumber(String orderNumber) {
        return repository.findByOrderNumber(orderNumber)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + orderNumber));
    }

    @Transactional
    public OrderResponse cancelOrder(String orderNumber) {
        Order order = findEntityByOrderNumber(orderNumber);

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Não é possível cancelar um pedido já enviado.");
        }

        // Reversão de stock 🔄
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(repository.save(order));
    }

    @Transactional
    public void updateOrderStatusByPaymentId(String externalId, OrderStatus newStatus) {
        Order order = repository.findByPaymentExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado para o pagamento: " + externalId));
        order.setStatus(newStatus);
        repository.save(order);
    }

    /**
     * MAPEADOR: Transforma Entidade em DTO com detalhes financeiros. 💎 ✨
     */
    public OrderResponse toResponse(Order order) {
        if (order == null) return null;

        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())         // Detalhe financeiro 💰
                .shippingFee(order.getShippingFee())   // Detalhe financeiro 🚚
                .discountAmount(order.getDiscountAmount()) // Detalhe financeiro 🏷️
                .totalAmount(order.getTotalAmount())
                .shippingMethod(order.getShippingMethod())
                .trackingCode(order.getTrackingCode())
                .items(itemResponses)
                .build();
    }
}