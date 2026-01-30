package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.review.ProductReviewRequest;
import com.hygor.makeup_api.dto.review.ProductReviewResponse;
import com.hygor.makeup_api.model.Product;
import com.hygor.makeup_api.model.ProductReview;
import com.hygor.makeup_api.repository.ProductRepository;
import com.hygor.makeup_api.repository.ProductReviewRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductReviewService extends BaseService<ProductReview, ProductReviewRepository> {

    private final ProductRepository productRepository;

    public ProductReviewService(ProductReviewRepository repository, ProductRepository productRepository) {
        super(repository);
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductReviewResponse createReview(ProductReviewRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        ProductReview review = ProductReview.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .product(product)
                .build();

        ProductReview saved = repository.save(review);
        log.info("Avaliação salva com ID: {}", saved.getId()); // Usa getId() herdado de BaseEntity
        
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductReviewResponse> findByProductId(Long productId) {
        // Busca direta no repository
        return repository.findByProductId(productId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

public ProductReviewResponse toResponse(ProductReview review) {
    if (review == null) return null;

    return ProductReviewResponse.builder()
            .id(review.getId()) // Agora o Builder vai encontrar o campo!
            .productId(review.getProduct() != null ? review.getProduct().getId() : null)
            .customerName(review.getUser() != null ? review.getUser().getFirstName() : "Cliente Anónimo")
            .rating(review.getRating())
            .comment(review.getComment())
            .createdAt(review.getCreatedAt())
            .build();
}
}