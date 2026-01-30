package com.hygor.makeup_api.dto.role;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Set;

@Data
public class RoleRequest {
    @NotBlank(message = "O nome do papel é obrigatório (ex: ROLE_ADMIN)")
    private String name;

    private Set<Long> permissionIds; // IDs das permissões granulares
}