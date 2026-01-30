package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.auth.AuthResponse;
import com.hygor.makeup_api.dto.auth.LoginRequest;
import com.hygor.makeup_api.model.User;
import com.hygor.makeup_api.repository.UserRepository;
import com.hygor.makeup_api.repository.RoleRepository; 
import com.hygor.makeup_api.security.JwtService;
import com.hygor.makeup_api.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Adicionado import

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; 
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    // 1. O PULO DO GATO: Injetar o MfaService aqui! 🕵️‍♀️ ✨
    private final MfaService mfaService;

    public String register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Este e-mail já está em uso.");
        }

        var customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Erro: Papel ROLE_CUSTOMER não encontrado no sistema."));

        user.getRoles().add(customerRole);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        
        return jwtService.generateToken(new UserPrincipal(user));
    }

    public AuthResponse authenticate(LoginRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Se o MFA estiver ativo, avisa o front-end
        if (user.isMfaEnabled()) {
            return AuthResponse.builder()
                    .mfaRequired(true)
                    .message("Por favor, insira o código do seu autenticador.")
                    .build();
        }

        String token = jwtService.generateToken(new UserPrincipal(user));
        return AuthResponse.builder()
                .token(token)
                .mfaRequired(false)
                .build();
    }

    /**
     * Validação Final: Troca o código TOTP pelo Token JWT definitivo. 💎 ✨
     */
    @Transactional
    public AuthResponse verifyMfaAndLogin(String email, int code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado."));

        // Verifica se o código que a Ana Julia digitou bate com o segredo no banco 🔐
        if (!mfaService.verifyCode(user.getSecretMfa(), code)) {
            throw new RuntimeException("Código MFA inválido ou expirado.");
        }

        // Se o código estiver certo, liberta o acesso total! 🛡️
        String token = jwtService.generateToken(new UserPrincipal(user));
        return AuthResponse.builder()
                .token(token)
                .mfaRequired(false)
                .build();
    }
}