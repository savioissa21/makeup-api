package com.hygor.makeup_api.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String brandName; // Nome correto para o Builder
    private String brandLogo; // Para exibir o logo no frontend
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Double rating;
    private String imageUrl;
    private String categoryName;
}