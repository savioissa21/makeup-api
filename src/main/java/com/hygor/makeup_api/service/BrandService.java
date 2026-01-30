package com.hygor.makeup_api.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hygor.makeup_api.dto.brand.BrandRequest;
import com.hygor.makeup_api.dto.brand.BrandResponse;
import com.hygor.makeup_api.model.Brand;
import com.hygor.makeup_api.repository.BrandRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BrandService extends BaseService<Brand, BrandRepository> {

    public BrandService(BrandRepository repository) {
        super(repository);
    }

    @Transactional
    public BrandResponse createBrand(BrandRequest request) {
        if (repository.existsByName(request.getName())) {
            throw new RuntimeException("Já existe uma marca com este nome.");
        }

        Brand brand = Brand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .slug(generateSlug(request.getName())) // Usa o mesmo gerador do ProductService
                .build();

        return mapToResponse(repository.save(brand));
    }

    public BrandResponse mapToResponse(Brand brand) {
        return BrandResponse.builder()
                .id(brand.getId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .description(brand.getDescription())
                .logoUrl(brand.getLogoUrl())
                .build();
    }
    
    private String generateSlug(String input) {
        // Lógica de normalização idêntica à do CategoryService
        return input.toLowerCase().replaceAll("\\s+", "-").replaceAll("[^\\w-]", "");
    }
}