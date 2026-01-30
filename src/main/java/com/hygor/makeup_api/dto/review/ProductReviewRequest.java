package com.hygor.makeup_api.dto.review;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductReviewRequest {
    @NotNull(message = "O ID do produto é obrigatório")
    private Long productId;

    @Min(value = 1, message = "A nota mínima é 1")
    @Max(value = 5, message = "A nota máxima é 5")
    private Integer rating;

    @NotBlank(message = "O comentário não pode estar vazio")
    private String comment;
}