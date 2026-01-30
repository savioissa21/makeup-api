package com.hygor.makeup_api.service;

import com.hygor.makeup_api.model.OrderStatus;
import com.hygor.makeup_api.model.ProductReview;
import com.hygor.makeup_api.repository.OrderRepository;
import com.hygor.makeup_api.repository.ProductRepository;
import com.hygor.makeup_api.repository.ProductReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductReviewService extends BaseService<ProductReview, ProductReviewRepository> {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public ProductReviewService(ProductReviewRepository repository, 
                                OrderRepository orderRepository, 
                                ProductRepository productRepository) {
        super(repository);
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    /**
     * Cria uma nova avaliação, validando a compra prévia e atualizando a média do produto.
     */
    @Transactional
    public ProductReview createReview(ProductReview review) {
        Long userId = review.getUser().getId();
        Long productId = review.getProduct().getId();

        // 1. Verificar se o utilizador já avaliou este produto
        if (repository.existsByUserIdAndProductId(userId, productId)) {
            throw new RuntimeException("Já avaliou este produto anteriormente.");
        }

        // 2. Verificar se o utilizador tem uma compra finalizada deste produto
        boolean hasPurchased = orderRepository.findAll().stream()
                .filter(order -> order.getCustomer().getId().equals(userId))
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .flatMap(order -> order.getItems().stream())
                .anyMatch(item -> item.getProduct().getId().equals(productId));

        if (!hasPurchased) {
            throw new RuntimeException("Apenas clientes que compraram e receberam o produto podem deixar uma avaliação.");
        }

        // 3. Guardar a avaliação
        ProductReview savedReview = repository.save(review);

        // 4. Recalcular e atualizar a média (rating) do produto
        updateProductAverageRating(productId);

        return savedReview;
    }

    private void updateProductAverageRating(Long productId) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

        List<ProductReview> reviews = repository.findByProductId(productId);
        
        double average = reviews.stream()
                .mapToDouble(ProductReview::getRating)
                .average()
                .orElse(0.0);

        product.setRating(average);
        productRepository.save(product);
    }
}