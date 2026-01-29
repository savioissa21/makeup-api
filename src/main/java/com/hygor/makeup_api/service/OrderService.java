package com.hygor.makeup_api.service;

import com.hygor.makeup_api.model.Order;
import com.hygor.makeup_api.model.OrderStatus;
import com.hygor.makeup_api.repository.OrderRepository;
import com.hygor.makeup_api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Gerencia a criação de pedidos e baixa de stock.
 */
@Service
public class OrderService extends BaseService<Order, OrderRepository> {

    private final ProductRepository productRepository;

    public OrderService(OrderRepository repository, ProductRepository productRepository) {
        super(repository);
        this.productRepository = productRepository;
    }

    @Transactional
    public Order createOrder(Order order) {
        // Gera número de pedido único
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.WAITING_PAYMENT);

        // Valida stock de cada item
        order.getItems().forEach(item -> {
            var product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Stock insuficiente para o produto: " + product.getName());
            }
            // Baixa o stock
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
            
            item.setOrder(order);
        });

        return repository.save(order);
    }
}