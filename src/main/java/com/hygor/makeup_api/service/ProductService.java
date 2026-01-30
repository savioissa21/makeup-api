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

    @Transactional(readOnly = true)
    public Product findBySlug(String slug) {
        return repository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com o slug: " + slug));
    }

 @Transactional(readOnly = true)
    public Page<Product> getFilteredProducts(String brand, BigDecimal minPrice, BigDecimal maxPrice, Double minRating, Pageable pageable) {
        // Define um valor padrão se o rating for nulo
        Double rating = (minRating == null) ? 0.0 : minRating;

        if (brand != null && minPrice != null && maxPrice != null) {
            return repository.findByBrandIgnoreCaseAndPriceBetweenAndRatingGreaterThanEqual(brand, minPrice, maxPrice, rating, pageable);
        } else if (minRating != null) {
            return repository.findByRatingGreaterThanEqual(rating, pageable);
        }
        
        return repository.findAll(pageable);
    }

    @Transactional
    public Product save(Product product) {
        // Lógica para gerar slug automaticamente se necessário
        if (product.getSlug() == null) {
            product.setSlug(product.getName().toLowerCase().replaceAll(" ", "-"));
        }
        return repository.save(product);
    }
}