package com.hygor.makeup_api.repository;

import com.hygor.makeup_api.model.Product;
import com.hygor.makeup_api.model.ProductReview;
import com.hygor.makeup_api.model.User;

import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductReviewRepository extends BaseEntityRepository<ProductReview, Long> {
    
    List<ProductReview> findByProductId(Long productId);
    
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    boolean existsByUserAndProduct(User user, Product product);
}