package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.order.OrderItemResponse;
import com.hygor.makeup_api.model.OrderItem;
import com.hygor.makeup_api.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderItemService {

    private final OrderItemRepository repository;

    /**
     * Procura todos os itens de uma encomenda específica e converte para DTO.
     */
    @Transactional(readOnly = true)
    public List<OrderItemResponse> findItemsByOrderId(Long orderId) {
        log.info("Buscando itens do pedido ID: {}", orderId);
        
        List<OrderItem> items = repository.findByOrderId(orderId);
        
        if (items.isEmpty()) {
            log.warn("Nenhum item encontrado para o pedido ID: {}", orderId);
        }

        return items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Mapeamento interno para garantir a integridade do DTO de resposta.
     */
    private OrderItemResponse mapToResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                .build();
    }
}