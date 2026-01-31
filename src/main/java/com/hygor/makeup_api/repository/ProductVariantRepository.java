package com.hygor.makeup_api.repository;

import com.hygor.makeup_api.model.ProductVariant;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends BaseEntityRepository<ProductVariant, Long> {
    Optional<ProductVariant> findBySku(String sku);

    List<ProductVariant> findByProductId(Long productId);

    Integer countByStockQuantityLessThan(Integer quantity);
}