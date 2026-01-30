package com.hygor.makeup_api.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRequest {
    @NotBlank(message = "O nome do produto é obrigatório")
    private String name;

    @NotBlank(message = "A marca é obrigatória")
    private String brand;

    private String description;

    @NotNull(message = "O preço é obrigatório")
    @PositiveOrZero
    private BigDecimal price;

    @PositiveOrZero
    private BigDecimal discountPrice;

    @NotNull(message = "A quantidade em estoque é obrigatória")
    @PositiveOrZero
    private Integer stockQuantity;

    private String imagePrompt; // Usado para a IA gerar a imagem real

    @NotNull(message = "O ID da categoria é obrigatório")
    private Long categoryId;
}