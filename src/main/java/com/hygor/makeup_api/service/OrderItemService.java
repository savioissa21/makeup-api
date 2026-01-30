package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.order.OrderItemResponse;
import com.hygor.makeup_api.model.OrderItem;
import com.hygor.makeup_api.model.Product;
import com.hygor.makeup_api.model.ProductVariant;
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
     * Mapeamento interno refatorado para suportar Variações (SKUs).
     * Garante que o nome do produto inclua a cor/tom para melhor UX.
     */
    private OrderItemResponse mapToResponse(OrderItem item) {
        // Acessamos a variante e o produto mestre para compor o nome completo
        ProductVariant variant = item.getVariant(); 
        Product product = variant.getProduct();

        return OrderItemResponse.builder()
                .variantId(variant.getId()) // Alterado para variantId para seguir o novo padrão de SKUs
                .productName(product.getName() + " - " + variant.getName()) // Ex: "Batom Matte - Vermelho Real"
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                .build();
    }
}