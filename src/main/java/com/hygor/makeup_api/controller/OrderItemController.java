package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.dto.order.OrderItemResponse;
import com.hygor.makeup_api.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order-items")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService orderItemService;

    /**
     * Lista todos os produtos e quantidades de um pedido específico.
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItemResponse>> getItemsByOrder(@PathVariable Long orderId) {
        // O Service já faz o mapeamento para OrderItemResponse (DTO)
        return ResponseEntity.ok(orderItemService.findItemsByOrderId(orderId));
    }
}