package com.hygor.makeup_api.service;

import com.hygor.makeup_api.model.Product;
import com.hygor.makeup_api.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class ProductService extends BaseService<Product, ProductRepository> {

    public ProductService(ProductRepository repository) {
        super(repository);
    }

    /**
     * Orquestra os filtros de busca para o front-end.
     * Se nenhum filtro for passado, retorna todos os produtos ativos.
     */
    @Transactional(readOnly = true)
    public Page<Product> getFilteredProducts(String brand, BigDecimal minPrice, BigDecimal maxPrice, Double minRating, Pageable pageable) {
        Double rating = (minRating == null) ? 0.0 : minRating;

        if (brand != null && minPrice != null && maxPrice != null) {
            return repository.findByBrandIgnoreCaseAndPriceBetweenAndRatingGreaterThanEqual(brand, minPrice, maxPrice, rating, pageable);
        } else if (brand != null) {
            return repository.findByBrandIgnoreCase(brand, pageable);
        } else if (minPrice != null && maxPrice != null) {
            return repository.findByPriceBetween(minPrice, maxPrice, pageable);
        } else if (minRating != null) {
            return repository.findByRatingGreaterThanEqual(rating, pageable);
        }

        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
public Page<Product> getPromotions(Pageable pageable) {
    return repository.findPromotions(pageable);
}

    @Transactional(readOnly = true)
    public Product findBySlug(String slug) {
        return repository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com o slug: " + slug));
    }

    @Transactional
    public Product save(Product product) {
        // Lógica de geração de slug mantida para consistência
        if (product.getSlug() == null) {
            product.setSlug(product.getName().toLowerCase().replaceAll(" ", "-"));
        }
        return repository.save(product);
    }
}