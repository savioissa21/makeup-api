package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.dto.variant.ProductVariantRequest;
import com.hygor.makeup_api.dto.variant.ProductVariantResponse;
import com.hygor.makeup_api.service.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Variações de Produto", description = "Endpoints para gerir cores e tons (SKUs)")
public class ProductVariantController {

    private final ProductVariantService variantService;

    @PostMapping("/{productId}/variants")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Adiciona uma cor/tom a um produto", description = "Cria um novo SKU vinculado ao produto mestre. Apenas ADMIN.")
    public ResponseEntity<ProductVariantResponse> create(
            @PathVariable Long productId,
            @Valid @RequestBody ProductVariantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(variantService.createVariant(productId, request));
    }

    @GetMapping("/{productId}/variants")
    @Operation(summary = "Lista todas as variações de um produto")
    public ResponseEntity<List<ProductVariantResponse>> getAllByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(variantService.findByProductId(productId));
    }

    @DeleteMapping("/variants/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove uma variação", description = "Realiza a exclusão lógica (soft delete) de um SKU.")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        variantService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}