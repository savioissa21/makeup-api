package com.hygor.makeup_api.dto.brand;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class BrandRequest {
    @NotBlank(message = "O nome da marca é obrigatório")
    private String name;
    private String description;
    private String logoUrl;
}