package com.hygor.makeup_api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.hygor.makeup_api.dto.variant.ProductVariantRequest;
import com.hygor.makeup_api.dto.variant.ProductVariantResponse;
import com.hygor.makeup_api.model.Product;
import com.hygor.makeup_api.model.ProductVariant;
import com.hygor.makeup_api.repository.ProductRepository;
import com.hygor.makeup_api.repository.ProductVariantRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductVariantService extends BaseService<ProductVariant, ProductVariantRepository> {

    private final ProductRepository productRepository;

    public ProductVariantService(ProductVariantRepository repository, ProductRepository productRepository) {
        super(repository);
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductVariantResponse createVariant(Long productId, ProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produto mestre não encontrado"));

        if (repository.findBySku(request.getSku()).isPresent()) {
            throw new RuntimeException("Já existe uma variação com este SKU.");
        }

        ProductVariant variant = ProductVariant.builder()
                .name(request.getName())
                .sku(request.getSku())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .product(product)
                .build();

        return toResponse(repository.save(variant));
    }

    public ProductVariantResponse toResponse(ProductVariant variant) {
        return ProductVariantResponse.builder()
                .id(variant.getId())
                .name(variant.getName())
                .sku(variant.getSku())
                .price(variant.getPrice())
                .stockQuantity(variant.getStockQuantity())
                .imageUrl(variant.getImageUrl())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ProductVariantResponse> findByProductId(Long productId) {
        // Busca as entidades no repositório e converte para DTOs de resposta
        return repository.findByProductId(productId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}