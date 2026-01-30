package com.hygor.makeup_api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    
    @NotBlank @Email 
    private String email;

    @NotBlank 
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
    private String password;
}