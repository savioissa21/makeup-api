package com.hygor.makeup_api.dto.product;

import com.hygor.makeup_api.dto.brand.BrandResponse;
import com.hygor.makeup_api.dto.category.CategoryResponse;
import com.hygor.makeup_api.dto.variant.ProductVariantResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Double rating;
    private String imageUrl;
    
    private BrandResponse brand; 
    private CategoryResponse category;
    private List<ProductVariantResponse> variants;
}