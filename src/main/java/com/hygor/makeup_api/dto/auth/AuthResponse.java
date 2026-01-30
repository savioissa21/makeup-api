package com.hygor.makeup_api.dto.auth;

import lombok.*;

@Getter @Setter // Usar explícito ajuda se o @Data falhar
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    
    @Builder.Default
    private String type = "Bearer";
    
    // ESTES SÃO OS CAMPOS QUE O AUTHSERVICE PRECISA 🕵️‍♀️ ✨
    private boolean mfaRequired;
    private String message;
}