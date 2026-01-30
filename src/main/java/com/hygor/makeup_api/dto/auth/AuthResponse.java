package com.hygor.makeup_api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    
    // NOVOS CAMPOS PARA MFA 🕵️‍♀️ ✨
    private boolean mfaRequired;
    private String message;
}