package com.hygor.makeup_api.dto.user;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data
@Builder
public class UserResponse {
    private String firstName;
    private String lastName;
    private String email;
    private Set<String> roles; // Apenas os nomes das permissões (ex: ROLE_CUSTOMER)
}