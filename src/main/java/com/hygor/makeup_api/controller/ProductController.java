// src/main/java/com/hygor/makeup_api/controller/ProductController.java
package com.hygor.makeup_api.controller;

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
public ResponseEntity<Page<Product>> listProducts(
        @RequestParam(required = false) String brand,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false) Double minRating,
        Pageable pageable) {
    
    return ResponseEntity.ok(productService.getFilteredProducts(brand, minPrice, maxPrice, minRating, pageable));
}

    @GetMapping("/{slug}")
    public ResponseEntity<Product> getBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.findBySlug(slug));
    }
} 