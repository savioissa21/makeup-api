package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.dto.cart.CartItemRequest;
import com.hygor.makeup_api.dto.cart.CartResponse;
import com.hygor.makeup_api.service.CartService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getMyCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItemToCart(request));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/apply-coupon")
@Operation(summary = "Aplica um cupão", description = "Adiciona um código de desconto ao carrinho atual do cliente.")
public ResponseEntity<CartResponse> applyCoupon(@RequestParam String code) {
    return ResponseEntity.ok(cartService.applyCoupon(code));
}
}