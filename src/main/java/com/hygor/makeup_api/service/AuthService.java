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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; 
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public String register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Este e-mail já está em uso.");
        }

        // 1. Busca o papel padrão de cliente no banco de dados
        var customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Erro: Papel ROLE_CUSTOMER não encontrado no sistema."));

        // 2. Atribui o papel ao utilizador
        // Certifique-se de que user.getRoles() não é null (no seu Model User.java ele está inicializado como HashSet)
        user.getRoles().add(customerRole);

        // 3. Criptografa a senha antes de salvar
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 4. Salva o utilizador com as suas permissões
        userRepository.save(user);
        
        return jwtService.generateToken(new UserPrincipal(user));
    }

public AuthResponse authenticate(LoginRequest request) {
    var user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

    // Valida a senha primeiro
    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
    );

    // Verifica se a Ana Julia ativou o MFA para este utilizador
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
}