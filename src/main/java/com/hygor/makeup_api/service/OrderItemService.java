package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.order.OrderItemResponse;
import com.hygor.makeup_api.mapper.OrderItemMapper; // Injeção
import com.hygor.makeup_api.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderItemService {

    private final OrderItemRepository repository;
    private final OrderItemMapper orderItemMapper;

    @Transactional(readOnly = true)
    public List<OrderItemResponse> findItemsByOrderId(Long orderId) {
        // O Mapper já trata a lógica de nome composto (Produto + Variante)
        return repository.findByOrderId(orderId).stream()
                .map(orderItemMapper::toResponse)
                .collect(Collectors.toList());
    }
}