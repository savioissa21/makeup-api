package com.hygor.makeup_api.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder; // Certifique-se de que este import existe
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder // Esta anotação habilita o método .builder()
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String brand;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Double rating;
    private String categoryName; // Nome da categoria para o front-end
}