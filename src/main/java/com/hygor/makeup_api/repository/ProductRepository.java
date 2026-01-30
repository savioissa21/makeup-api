package com.hygor.makeup_api.repository;

import com.hygor.makeup_api.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Interface de acesso a dados para a entidade Product.
 * Inclui suporte a paginação e busca por SEO (slug).
 */
@Repository
public interface ProductRepository extends BaseEntityRepository<Product, Long> {

    // Busca um produto pelo slug para exibir na página de detalhes do site
    Optional<Product> findBySlug(String slug);

    // Lista produtos por categoria com suporte a paginação (Performance)
    Page<Product> findByCategorySlug(String categorySlug, Pageable pageable);

    // Busca produtos que contenham parte do nome (Barra de pesquisa)
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByBrandIgnoreCase(String brand, Pageable pageable);

    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    Page<Product> findByBrandIgnoreCaseAndPriceBetween(String brand, BigDecimal minPrice, BigDecimal maxPrice,
            Pageable pageable);

    Page<Product> findByRatingGreaterThanEqual(Double minRating, Pageable pageable);

    Page<Product> findByBrandIgnoreCaseAndPriceBetweenAndRatingGreaterThanEqual(
            String brand, BigDecimal minPrice, BigDecimal maxPrice, Double minRating, Pageable pageable);

    Page<Product> findByBrandNameIgnoreCaseAndPriceBetweenAndRatingGreaterThanEqual(
            String brandName, BigDecimal minPrice, BigDecimal maxPrice, Double minRating, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.discountPrice IS NOT NULL AND p.discountPrice < p.price")
    Page<Product> findPromotions(Pageable pageable);

}