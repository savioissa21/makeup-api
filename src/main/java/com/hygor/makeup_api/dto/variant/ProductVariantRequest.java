package com.hygor.makeup_api.dto.variant;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ProductVariantRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String sku;
    @NotNull
    @PositiveOrZero
    private BigDecimal price;
    @NotNull
    @PositiveOrZero
    private Integer stockQuantity;
    private String imageUrl;
}