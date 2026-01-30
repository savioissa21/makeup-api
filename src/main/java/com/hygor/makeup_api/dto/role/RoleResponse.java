package com.hygor.makeup_api.dto.role;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class RoleResponse {
    private Long id;
    private String name;
    private Set<String> permissions; // Nomes das permissões para o front-end
}