package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.review.ProductReviewRequest;
import com.hygor.makeup_api.dto.review.ProductReviewResponse;
import com.hygor.makeup_api.model.Product;
import com.hygor.makeup_api.model.ProductReview;
import com.hygor.makeup_api.model.User;
import com.hygor.makeup_api.repository.ProductReviewRepository;
import com.hygor.makeup_api.repository.ProductRepository;
import com.hygor.makeup_api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductReviewService extends BaseService<ProductReview, ProductReviewRepository> {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductReviewService(ProductReviewRepository repository, 
                                ProductRepository productRepository, 
                                UserRepository userRepository) {
        super(repository);
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Cria uma avaliação e atualiza a média de rating do produto.
     */
    @Transactional
    public ProductReviewResponse createReview(ProductReviewRequest request) {
        // 1. Identifica o utilizador logado
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));

        // 2. Busca o produto
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // 3. Verifica se o utilizador já avaliou este produto (Prevenção de Spam)
        if (repository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("Você já avaliou este produto.");
        }

        // 4. Cria a avaliação
        ProductReview review = ProductReview.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .product(product)
                .user(user)
                .build();

        repository.save(review);
        
        // 5. Atualiza a nota média do produto
        updateProductRating(product);

        log.info("Nova avaliação para o produto {}: {} estrelas", product.getName(), request.getRating());
        return mapToResponse(review);
    }

    /**
     * Retorna todas as avaliações de um produto específico.
     */
    @Transactional(readOnly = true)
    public List<ProductReviewResponse> getProductReviews(Long productId) {
        return repository.findByProductId(productId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Calcula e atualiza a média de rating do produto no banco.
     */
    private void updateProductRating(Product product) {
        List<ProductReview> reviews = repository.findByProductId(product.getId());
        double average = reviews.stream()
                .mapToInt(ProductReview::getRating)
                .average()
                .orElse(0.0);
        
        product.setRating(average);
        productRepository.save(product);
    }

    private ProductReviewResponse mapToResponse(ProductReview review) {
        return ProductReviewResponse.builder()
                .customerName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}