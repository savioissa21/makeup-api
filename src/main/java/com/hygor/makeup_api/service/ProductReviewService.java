package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.review.ProductReviewRequest;
import com.hygor.makeup_api.dto.review.ProductReviewResponse;
import com.hygor.makeup_api.model.Product;
import com.hygor.makeup_api.model.ProductReview;
import com.hygor.makeup_api.model.User; // 1. IMPORTADO O MODELO CORRETO
import com.hygor.makeup_api.repository.OrderRepository; // NOVO
import com.hygor.makeup_api.repository.ProductRepository;
import com.hygor.makeup_api.repository.ProductReviewRepository;
import com.hygor.makeup_api.repository.UserRepository; // 2. IMPORTADO O REPOSITORY
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
    private final OrderRepository orderRepository; // Injetado para a verificação

    public ProductReviewService(ProductReviewRepository repository, 
                                ProductRepository productRepository, 
                                UserRepository userRepository,
                                OrderRepository orderRepository) {
        super(repository);
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

   @Transactional
    public ProductReviewResponse createReview(ProductReviewRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 1. REGRA DE OURO: Bloqueia avaliações sem compra confirmada 🛡️ ✨
        boolean hasPurchased = orderRepository.hasPurchasedProduct(email, request.getProductId());
        if (!hasPurchased) {
            throw new RuntimeException("Apenas clientes que compraram e receberam este produto podem avaliá-lo.");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        User user = userRepository.findByEmail(email).orElseThrow();

        // 2. Criação da Avaliação
        ProductReview review = ProductReview.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .product(product)
                .user(user)
                .build();

        ProductReview saved = repository.save(review);

        // 3. Atualização automática da média do produto (Calculada pelo PostgreSQL) ⚡
        List<ProductReview> allReviews = repository.findByProductId(product.getId());
        double average = allReviews.stream()
                .mapToInt(ProductReview::getRating)
                .average()
                .orElse(0.0);

        product.setRating(average);
        productRepository.save(product);

        log.info("Avaliação verificada salva para {}. Nova média: {}", product.getName(), average);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductReviewResponse> findByProductId(Long productId) {
        return repository.findByProductId(productId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProductReviewResponse toResponse(ProductReview review) {
        if (review == null) return null;

        // Garante que o DTO de resposta oculte dados sensíveis do User
        return ProductReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct() != null ? review.getProduct().getId() : null)
                .customerName(review.getUser() != null ? review.getUser().getFirstName() : "Cliente Anónimo")
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}