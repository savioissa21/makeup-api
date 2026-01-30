package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.dto.product.ProductResponse;
import com.hygor.makeup_api.service.FileStorageService;
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
    private final FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            Pageable pageable) {
        
        // CORREÇÃO: O serviço agora já retorna Page<ProductResponse>, 
        // então não é necessário usar o .map() novamente.
        return ResponseEntity.ok(
            productService.getFilteredProducts(brand, minPrice, maxPrice, minRating, pageable)
        );
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ProductResponse> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(productService.findBySlug(slug)); 
    }

    @PostMapping("/{id}/upload-image")
    @Operation(summary = "Upload de foto do produto", description = "Guarda a foto real da maquiagem no servidor.")
    public ResponseEntity<ProductResponse> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.saveImage(file);
        String imageUrl = "/uploads/" + fileName;
        
        return ResponseEntity.ok(productService.updateProductImage(id, imageUrl));
    }
}