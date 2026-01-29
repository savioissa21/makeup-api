package com.hygor.makeup_api.service;

import com.hygor.makeup_api.model.OrderItem;
import com.hygor.makeup_api.repository.OrderItemRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderItemService {

    private final OrderItemRepository repository;

    public OrderItemService(OrderItemRepository repository) {
        this.repository = repository;
    }

    public List<OrderItem> findByOrderId(Long orderId) {
        return repository.findByOrderId(orderId);
    }
}