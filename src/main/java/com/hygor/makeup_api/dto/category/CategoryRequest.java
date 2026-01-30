package com.hygor.makeup_api.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "O nome da categoria é obrigatório")
    private String name;

    private String description;
}