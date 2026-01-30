package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.order.OrderRequest;
import com.hygor.makeup_api.dto.order.OrderResponse;
import com.hygor.makeup_api.dto.order.OrderItemResponse;
import com.hygor.makeup_api.model.*;
import com.hygor.makeup_api.repository.OrderRepository;
import com.hygor.makeup_api.repository.ProductRepository;
import com.hygor.makeup_api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
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
     * Cria um novo pedido com validações robustas de estoque, preço e segurança.
     */
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // 1. Identifica o cliente logado via Token JWT (Segurança Máxima)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com o e-mail: " + email));

        // 2. Inicializa a entidade Order com dados básicos
        Order order = Order.builder()
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.WAITING_PAYMENT)
                .customer(customer)
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        // 3. Processa cada item do pedido vindo do DTO
        for (var itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado ID: " + itemRequest.getProductId()));

            // Validação de Stock: Garante que a boutique pode entregar o prometido
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + product.getName());
            }

            // Baixa de Stock: Atualiza o inventário imediatamente
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);

            // Criação do item do pedido com Segurança de Preço (Usa o valor atual do banco)
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .order(order)
                    .build();

            order.getItems().add(orderItem);

            // Soma ao total do pedido (unitPrice do banco * quantidade)
            BigDecimal itemTotal = orderItem.getUnitPrice().multiply(new BigDecimal(orderItem.getQuantity()));
            total = total.add(itemTotal);
        }

        order.setTotalAmount(total);
        Order savedOrder = repository.save(order);
        
        log.info("Pedido {} criado com sucesso para o cliente {}", savedOrder.getOrderNumber(), email);
        return mapToResponse(savedOrder);
    }

    /**
     * Mapeia a Entidade Order para o DTO OrderResponse.
     */
    private OrderResponse mapToResponse(Order order) {
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