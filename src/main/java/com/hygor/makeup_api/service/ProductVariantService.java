package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.variant.ProductVariantRequest;
import com.hygor.makeup_api.dto.variant.ProductVariantResponse;
import com.hygor.makeup_api.exception.custom.BusinessException;
import com.hygor.makeup_api.exception.custom.ResourceNotFoundException;
import com.hygor.makeup_api.mapper.ProductVariantMapper; // Injeção
import com.hygor.makeup_api.model.Product;
import com.hygor.makeup_api.model.ProductVariant;
import com.hygor.makeup_api.repository.ProductRepository;
import com.hygor.makeup_api.repository.ProductVariantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductVariantService extends BaseService<ProductVariant, ProductVariantRepository> {

    private final ProductRepository productRepository;
    private final ProductVariantMapper productVariantMapper; // Injeção

    public ProductVariantService(ProductVariantRepository repository,
                                 ProductRepository productRepository,
                                 ProductVariantMapper productVariantMapper) {
        super(repository);
        this.productRepository = productRepository;
        this.productVariantMapper = productVariantMapper;
    }

    @Transactional
    public ProductVariantResponse createVariant(Long productId, ProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto mestre não encontrado ID: " + productId));

        if (repository.findBySku(request.getSku()).isPresent()) {
            throw new BusinessException("Já existe uma variação com este SKU: " + request.getSku());
        }

        ProductVariant variant = ProductVariant.builder()
                .name(request.getName())
                .sku(request.getSku())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .product(product)
                .build();

        ProductVariant saved = repository.save(variant);
        log.info("Nova variação criada: {} (SKU: {})", saved.getName(), saved.getSku());
        
        return productVariantMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductVariantResponse> findByProductId(Long productId) {
        return repository.findByProductId(productId).stream()
                .map(productVariantMapper::toResponse)
                .collect(Collectors.toList());
    }
}