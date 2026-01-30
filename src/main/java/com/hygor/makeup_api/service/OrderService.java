package com.hygor.makeup_api.service;

import com.hygor.makeup_api.model.Order;
import com.hygor.makeup_api.model.OrderStatus;
import com.hygor.makeup_api.repository.OrderRepository;
import com.hygor.makeup_api.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderService extends BaseService<Order, OrderRepository> {

    private final ProductRepository productRepository;

    public OrderService(OrderRepository repository, ProductRepository productRepository) {
        super(repository);
        this.productRepository = productRepository;
    }

    @Transactional
    public Order createOrder(Order order) {
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.WAITING_PAYMENT);

        // Inicializa o total como zero
        BigDecimal total = BigDecimal.ZERO;

        for (var item : order.getItems()) {
            // Busca o produto atualizado do banco para garantir preço e estoque reais
            var product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

            // 1. Validação de Stock
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Stock insuficiente para: " + product.getName());
            }

            // 2. Baixa de Stock
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);

            // 3. Segurança de Preço: Usa o preço do banco, não o que veio no JSON
            item.setUnitPrice(product.getPrice());
            item.setProduct(product);
            item.setOrder(order);

            // 4. Soma ao total do pedido
            BigDecimal itemTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
            total = total.add(itemTotal);
        }

        order.setTotalAmount(total);
        return repository.save(order);
    }
}