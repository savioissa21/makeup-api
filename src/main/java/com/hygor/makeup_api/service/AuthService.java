package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.auth.AuthResponse;
import com.hygor.makeup_api.dto.auth.LoginRequest;
import com.hygor.makeup_api.exception.custom.BusinessException;
import com.hygor.makeup_api.exception.custom.ResourceNotFoundException;
import com.hygor.makeup_api.model.Role;
import com.hygor.makeup_api.model.User;
import com.hygor.makeup_api.repository.RoleRepository;
import com.hygor.makeup_api.repository.UserRepository;
import com.hygor.makeup_api.security.JwtService;
import com.hygor.makeup_api.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final MfaService mfaService;

    @Transactional
    public String register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException("Este e-mail já está em uso.");
        }

        // Busca a Role com tratamento de erro adequado
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Erro de Configuração: Papel ROLE_CUSTOMER não encontrado."));

        user.setRoles(Collections.singleton(customerRole));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        User savedUser = userRepository.save(user);
        log.info("Novo usuário registrado: {}", savedUser.getEmail());

        return jwtService.generateToken(new UserPrincipal(savedUser));
    }

    public AuthResponse authenticate(LoginRequest request) {
        // 1. Tenta autenticar via Spring Security (Lança BadCredentialsException se falhar)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. Busca o usuário (Se passou do passo 1, o email existe, mas validamos por segurança)
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        // 3. Verifica MFA
        if (user.isMfaEnabled()) {
            return AuthResponse.builder()
                    .mfaRequired(true)
                    .message("MFA Ativado. Por favor, insira o código do seu autenticador.")
                    .build();
        }

        // 4. Gera Token
        String token = jwtService.generateToken(new UserPrincipal(user));
        return AuthResponse.builder()
                .token(token)
                .mfaRequired(false)
                .build();
    }

    @Transactional
    public AuthResponse verifyMfaAndLogin(String email, int code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + email));

        // Verifica validade do código TOTP
        if (!mfaService.verifyCode(user.getSecretMfa(), code)) {
            // BusinessException = 422 (Regra de Negócio/Dado Inválido)
            throw new BusinessException("Código MFA inválido ou expirado. Tente novamente.");
        }

        String token = jwtService.generateToken(new UserPrincipal(user));
        log.info("Login MFA bem-sucedido para: {}", email);

        return AuthResponse.builder()
                .token(token)
                .mfaRequired(false)
                .build();
    }
}