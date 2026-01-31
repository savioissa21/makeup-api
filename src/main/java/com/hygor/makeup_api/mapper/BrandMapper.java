package com.hygor.makeup_api.mapper;

import com.hygor.makeup_api.dto.brand.BrandResponse;
import com.hygor.makeup_api.model.Brand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    BrandResponse toResponse(Brand brand);
}