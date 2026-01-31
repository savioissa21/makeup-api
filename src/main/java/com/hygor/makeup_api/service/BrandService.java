package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.brand.BrandRequest;
import com.hygor.makeup_api.dto.brand.BrandResponse;
import com.hygor.makeup_api.exception.custom.BusinessException;
import com.hygor.makeup_api.mapper.BrandMapper; // Novo
import com.hygor.makeup_api.model.Brand;
import com.hygor.makeup_api.repository.BrandRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class BrandService extends BaseService<Brand, BrandRepository> {

    private final BrandMapper brandMapper; // Injeção do Mapper

    public BrandService(BrandRepository repository, BrandMapper brandMapper) {
        super(repository);
        this.brandMapper = brandMapper;
    }

    @Transactional
    public BrandResponse createBrand(BrandRequest request) {
        if (repository.existsByName(request.getName())) {
            // Exceção correta: Retorna 422 Unprocessable Entity
            throw new BusinessException("Já existe uma marca com este nome.");
        }

        Brand brand = Brand.builder()
                .name(request.getName())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .slug(generateSlug(request.getName()))
                .build();

        // O Mapper faz a conversão automática 🪄
        return brandMapper.toResponse(repository.save(brand));
    }

    // Método auxiliar para gerar slug (podes mover para uma classe utilitária depois)
    private String generateSlug(String input) {
        return input.toLowerCase().replaceAll("\\s+", "-").replaceAll("[^\\w-]", "");
    }
}