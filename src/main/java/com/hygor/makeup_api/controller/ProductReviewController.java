package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.model.ProductReview;
import com.hygor.makeup_api.service.ProductReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService reviewService;

    @PostMapping
    public ResponseEntity<ProductReview> create(@Valid @RequestBody ProductReview review) {
        ProductReview created = reviewService.createReview(review);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductReview>> getByProduct(@PathVariable Long productId) {
        // CORRIGIDO: Agora usa o método público do serviço
        return ResponseEntity.ok(reviewService.findByProductId(productId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}