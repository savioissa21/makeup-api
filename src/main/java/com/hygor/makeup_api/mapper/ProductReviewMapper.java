package com.hygor.makeup_api.mapper;

import com.hygor.makeup_api.dto.review.ProductReviewResponse;
import com.hygor.makeup_api.model.ProductReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductReviewMapper {

    // Extrai o nome completo do cliente a partir da relação User
    @Mapping(target = "customerName", expression = "java(review.getUser().getFirstName() + \" \" + review.getUser().getLastName())")
    // Mapeia o ID do produto para facilitar a navegação no frontend
    @Mapping(target = "productId", source = "product.id")
    ProductReviewResponse toResponse(ProductReview review);
}