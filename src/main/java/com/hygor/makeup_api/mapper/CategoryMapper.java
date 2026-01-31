package com.hygor.makeup_api.mapper;

import com.hygor.makeup_api.dto.category.CategoryResponse;
import com.hygor.makeup_api.model.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse toResponse(Category category);
}