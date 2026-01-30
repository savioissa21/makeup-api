package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.dto.coupon.CouponRequest;
import com.hygor.makeup_api.dto.coupon.CouponResponse;
import com.hygor.makeup_api.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "Cupons", description = "Gestão de promoções e descontos da boutique")
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria um novo cupão", description = "Permite ao administrador criar códigos de desconto. Apenas ADMIN.")
    public ResponseEntity<CouponResponse> create(@Valid @RequestBody CouponRequest request) {
        return ResponseEntity.ok(couponService.createCoupon(request));
    }

    @GetMapping("/{code}")
    @Operation(summary = "Verifica um cupão", description = "Busca detalhes e validade de um cupão pelo código.")
    public ResponseEntity<CouponResponse> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(couponService.findByCode(code));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista todos os cupons", description = "Retorna todos os cupons configurados no sistema. Apenas ADMIN.")
    public ResponseEntity<List<CouponResponse>> getAll() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Apaga um cupão", description = "Realiza a exclusão lógica do cupão pelo ID. Apenas ADMIN.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        couponService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}