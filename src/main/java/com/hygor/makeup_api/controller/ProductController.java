package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.dto.product.ProductResponse;
import com.hygor.makeup_api.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    // REMOVIDO: private final LocalStorageAdapter fileStorageService; 
    // O Controller não mexe mais com arquivos diretamente.

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Pageable pageable) {
        
        return ResponseEntity.ok(
            productService.getFilteredProducts(brand, minPrice, maxPrice, minRating, pageable)
        );
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.findBySlug(slug)); 
    }

    @PostMapping("/{id}/upload-image")
    @Operation(summary = "Upload de foto do produto", description = "Envia a foto para o armazenamento (Local ou Cloudinary).")
    public ResponseEntity<ProductResponse> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        // CORREÇÃO: Passamos o arquivo direto.
        // O ProductService vai chamar o Gateway, que decide se salva no Disco ou no Cloudinary.
        return ResponseEntity.ok(productService.updateProductImage(id, file));
    }
}