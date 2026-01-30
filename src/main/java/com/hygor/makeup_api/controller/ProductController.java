package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.dto.product.ProductResponse;
import com.hygor.makeup_api.model.Product;
import com.hygor.makeup_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            Pageable pageable) {
        
        // CORREÇÃO: Chamamos o serviço e mapeamos cada Produto para ProductResponse (DTO)
        // Isso resolve o erro de "incompatible types" que viste no log!
        return ResponseEntity.ok(
            productService.getFilteredProducts(brand, minPrice, maxPrice, minRating, pageable)
                .map(productService::toResponse)
        );
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlug(@PathVariable String slug) {
        // CORREÇÃO: Busca o produto pelo slug e já retorna como DTO
        return ResponseEntity.ok(productService.findBySlug(slug)); 
    }
}