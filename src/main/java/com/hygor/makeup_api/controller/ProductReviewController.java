package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.dto.review.ProductReviewRequest;
import com.hygor.makeup_api.dto.review.ProductReviewResponse;
import com.hygor.makeup_api.service.ProductReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ProductReviewController {

    private final ProductReviewService reviewService;

    @PostMapping
    public ResponseEntity<ProductReviewResponse> createReview(@Valid @RequestBody ProductReviewRequest request) {
        // Agora o tipo de entrada (Request) e saída (Response) bate com o Service!
        return ResponseEntity.ok(reviewService.createReview(request));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductReviewResponse>> getReviewsByProduct(@PathVariable Long productId) {
        // Chama o novo método findByProductId que adicionámos ao Service
        return ResponseEntity.ok(reviewService.findByProductId(productId));
    }
}