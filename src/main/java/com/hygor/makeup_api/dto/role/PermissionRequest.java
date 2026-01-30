package com.hygor.makeup_api.dto.role;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionRequest {
    @NotBlank(message = "O nome da permissão é obrigatório (ex: MANAGE_PRODUCTS)")
    private String name;
}