package com.hygor.makeup_api.service;

import com.hygor.makeup_api.model.Product;
import com.hygor.makeup_api.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Page<Product> searchByName(String name, Pageable pageable) {
        return repository.findByNameContainingIgnoreCase(name, pageable);
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