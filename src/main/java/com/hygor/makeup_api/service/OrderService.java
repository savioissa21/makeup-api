package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.order.OrderRequest;
import com.hygor.makeup_api.dto.order.OrderResponse;
import com.hygor.makeup_api.dto.order.OrderItemResponse;
import com.hygor.makeup_api.model.*;
import com.hygor.makeup_api.repository.OrderRepository;
import com.hygor.makeup_api.repository.ProductRepository;
import com.hygor.makeup_api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

// CORREÇÃO DOS IMPORTS: Use sempre org.springframework.data.domain
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

    public OrderService(OrderRepository repository, ProductRepository productRepository, UserRepository userRepository) {
        super(repository);
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retorna o histórico de pedidos do utilizador logado em formato DTO.
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // O .map(this::toResponse) garante que retornamos DTOs, não Entidades
        return repository.findByCustomerEmail(email, pageable).map(this::toResponse);
    }

    /**
     * Busca um pedido específico pelo número em formato DTO.
     */
    @Transactional(readOnly = true)
    public OrderResponse getByOrderNumber(String orderNumber) {
        return repository.findByOrderNumber(orderNumber)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado: " + orderNumber));
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com o e-mail: " + email));

        Order order = Order.builder()
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.WAITING_PAYMENT)
                .customer(customer)
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (var itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado ID: " + itemRequest.getProductId()));

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .order(order)
                    .build();

            order.getItems().add(orderItem);

            BigDecimal itemTotal = orderItem.getUnitPrice().multiply(new BigDecimal(orderItem.getQuantity()));
            total = total.add(itemTotal);
        }

        order.setTotalAmount(total);
        Order savedOrder = repository.save(order);
        
        log.info("Pedido {} criado com sucesso para o cliente {}", savedOrder.getOrderNumber(), email);
        return toResponse(savedOrder);
    }

    @Transactional
    public void updateOrderStatusByPaymentId(String externalId, OrderStatus newStatus) {
        // Busca direta via ID do Mercado Pago
        Order order = repository.findByPaymentExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado para o pagamento: " + externalId));
        
        order.setStatus(newStatus);
        repository.save(order);
        log.info("Pedido {} atualizado para o status {}", order.getOrderNumber(), newStatus);
    }

    /**
     * Mapeador público para seguir o padrão da boutique.
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
                .totalAmount(order.getTotalAmount())
                .trackingCode(order.getTrackingCode())
                .items(itemResponses)
                .build();
    }
}