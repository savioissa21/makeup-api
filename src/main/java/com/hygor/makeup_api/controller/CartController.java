package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.dto.cart.CartItemRequest;
import com.hygor.makeup_api.dto.cart.CartResponse;
import com.hygor.makeup_api.service.CartItemService;
import com.hygor.makeup_api.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CartItemService cartItemService;

    @GetMapping
    public ResponseEntity<CartResponse> getMyCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@RequestBody @Valid CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItemToCart(request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<Void> updateItemQuantity(@PathVariable Long itemId, @RequestParam Integer quantity) {
        cartItemService.updateQuantity(itemId, quantity);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long itemId) {
        cartItemService.removeItem(itemId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/coupon/{code}")
    public ResponseEntity<CartResponse> applyCoupon(@PathVariable String code) {
        return ResponseEntity.ok(cartService.applyCoupon(code));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}