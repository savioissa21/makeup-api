package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.dto.auth.AuthResponse;
import com.hygor.makeup_api.dto.auth.LoginRequest;
import com.hygor.makeup_api.dto.auth.RegisterRequest;
import com.hygor.makeup_api.model.User;
import com.hygor.makeup_api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para registo, login e segurança MFA")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Regista um novo cliente", description = "Cria uma conta padrão (ROLE_CUSTOMER) e devolve o token inicial.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Converte o DTO para a Entidade User antes de enviar para o serviço
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(request.getPassword())
                .build();
        
        String token = authService.register(user); //
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .mfaRequired(false)
                .build());
    }

    @PostMapping("/authenticate")
    @Operation(summary = "Realiza o login", description = "Valida as credenciais. Se o MFA estiver ativo, devolve 'mfaRequired: true' em vez do token.")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request)); //
    }

    @PostMapping("/mfa-verify")
    @Operation(summary = "Verifica código MFA", description = "Valida o código do Google Authenticator para concluir o login e libertar o Token.")
    public ResponseEntity<AuthResponse> verifyMfa(@RequestParam String email, @RequestParam int code) {
        // Chama o método que criámos para a verificação final 🕵️‍♀️ ✨
        return ResponseEntity.ok(authService.verifyMfaAndLogin(email, code));
    }
}