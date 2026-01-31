package com.hygor.makeup_api.mapper;

import com.hygor.makeup_api.dto.product.ProductResponse;
import com.hygor.makeup_api.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {BrandMapper.class, CategoryMapper.class, ProductVariantMapper.class})
public interface ProductMapper {

    // O MapStruct chama automaticamente os mappers de Brand e Category definidos em 'uses'
    // A expressão java permite chamar o método da entidade para o stock total
    @Mapping(target = "totalStock", expression = "java(product.getTotalStockQuantity())")
    ProductResponse toResponse(Product product);
}