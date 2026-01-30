package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart-items")
@RequiredArgsConstructor
public class CartItemController {

    private final CartItemService cartItemService;

    /**
     * Atualiza a quantidade de um item específico que já está no carrinho.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateQuantity(@PathVariable Long id, @RequestParam Integer quantity) {
        cartItemService.updateQuantity(id, quantity);
        return ResponseEntity.ok().build();
    }

    /**
     * Remove um item específico do carrinho através do seu ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeItem(@PathVariable Long id) {
        cartItemService.removeItem(id);
        return ResponseEntity.noContent().build();
    }
}