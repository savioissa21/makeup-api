package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.dto.auth.AuthResponse;
import com.hygor.makeup_api.dto.auth.LoginRequest;
import com.hygor.makeup_api.dto.auth.ResetPasswordRequest; // Criar este DTO simples (token, newPassword)
import com.hygor.makeup_api.model.User;
import com.hygor.makeup_api.service.AuthService;
import com.hygor.makeup_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid User user) {
        String token = authService.register(user);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/verify-mfa")
    public ResponseEntity<AuthResponse> verifyMfa(@RequestParam String email, @RequestParam int code) {
        return ResponseEntity.ok(authService.verifyMfaAndLogin(email, code));
    }

    // --- RECUPERAÇÃO DE SENHA 🆕 ---

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestParam String email) {
        userService.forgotPassword(email);
        // Retornamos 200 OK sempre por segurança (para não revelar se o email existe)
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}