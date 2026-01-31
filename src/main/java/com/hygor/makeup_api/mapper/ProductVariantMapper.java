package com.hygor.makeup_api.mapper;

import com.hygor.makeup_api.dto.variant.ProductVariantResponse;
import com.hygor.makeup_api.model.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductVariantMapper {

    // Se o teu DTO tiver productId, mapeamos assim:
    @Mapping(target = "productId", source = "product.id")
    ProductVariantResponse toResponse(ProductVariant variant);
}