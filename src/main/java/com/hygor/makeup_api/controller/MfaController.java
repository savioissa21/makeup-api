package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mfa")
@RequiredArgsConstructor
public class MfaController {

    private final UserService userService;

    @GetMapping("/setup")
    @Operation(summary = "Gera QR Code", description = "Retorna a URL do QR Code para configurar o MFA no Google Authenticator.")
    public ResponseEntity<String> setupMfa() {
        return ResponseEntity.ok(userService.generateMfaQrCode());
    }

    @PostMapping("/enable")
    @Operation(summary = "Ativa o MFA", description = "Valida o código do telemóvel e ativa permanentemente o MFA na conta.")
    public ResponseEntity<Void> enableMfa(@RequestParam int code) {
        userService.enableMfa(code);
        return ResponseEntity.ok().build();
    }
}