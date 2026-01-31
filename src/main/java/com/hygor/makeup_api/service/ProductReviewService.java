package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.review.ProductReviewRequest;
import com.hygor.makeup_api.dto.review.ProductReviewResponse;
import com.hygor.makeup_api.exception.custom.BusinessException;
import com.hygor.makeup_api.exception.custom.ResourceNotFoundException;
import com.hygor.makeup_api.mapper.ProductReviewMapper; // Injeção
import com.hygor.makeup_api.model.Product;
import com.hygor.makeup_api.model.ProductReview;
import com.hygor.makeup_api.model.User;
import com.hygor.makeup_api.repository.OrderRepository;
import com.hygor.makeup_api.repository.ProductRepository;
import com.hygor.makeup_api.repository.ProductReviewRepository;
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
    private final OrderRepository orderRepository;
    private final ProductReviewMapper productReviewMapper; // Injeção

    public ProductReviewService(ProductReviewRepository repository,
                                ProductRepository productRepository,
                                UserRepository userRepository,
                                OrderRepository orderRepository,
                                ProductReviewMapper productReviewMapper) {
        super(repository);
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.productReviewMapper = productReviewMapper;
    }

    @Transactional
    public ProductReviewResponse createReview(ProductReviewRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 1. Validação de Compra 🛡️
        boolean hasPurchased = orderRepository.hasPurchasedProduct(email, request.getProductId());
        if (!hasPurchased) {
            throw new BusinessException("Apenas clientes que compraram e receberam este produto podem avaliá-lo.");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado ID: " + request.getProductId()));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        // 2. Salva a Avaliação
        ProductReview review = ProductReview.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .product(product)
                .user(user)
                .build();

        ProductReview saved = repository.save(review);

        // 3. Atualiza a Média do Produto
        updateProductRating(product);

        log.info("Avaliação salva para produto {}. Nota: {}", product.getName(), request.getRating());
        return productReviewMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductReviewResponse> findByProductId(Long productId) {
        return repository.findByProductId(productId).stream()
                .map(productReviewMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Recalcula e atualiza a média de estrelas do produto.
     */
    private void updateProductRating(Product product) {
        List<ProductReview> allReviews = repository.findByProductId(product.getId());
        double average = allReviews.stream()
                .mapToInt(ProductReview::getRating)
                .average()
                .orElse(0.0);

        product.setRating(average);
        productRepository.save(product);
    }
}