package com.hygor.makeup_api.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO para o fluxo de segurança de alteração de senha.
 */
@Data
public class ChangePasswordRequest {

    @NotBlank(message = "A senha antiga é obrigatória para verificação")
    private String oldPassword;

    @NotBlank(message = "A nova senha é obrigatória")
    @Size(min = 6, message = "A nova senha deve ter no mínimo 6 caracteres")
    private String newPassword;
}