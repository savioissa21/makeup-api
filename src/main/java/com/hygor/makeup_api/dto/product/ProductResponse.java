package com.hygor.makeup_api.dto.product;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductResponse {
    private String name;
    private String brand;
    private String slug;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Double rating;
}