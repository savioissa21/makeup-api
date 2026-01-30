package com.hygor.makeup_api.service;

import com.hygor.makeup_api.model.User;
import com.hygor.makeup_api.repository.UserRepository;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public String register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Este e-mail já está em uso.");
        }
        
        // Criptografa a senha antes de salvar
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        
        return jwtService.generateToken(new UserPrincipal(user));
    }

    public String authenticate(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
        
        // Converte para o DTO de segurança antes de gerar o token
        return jwtService.generateToken(new UserPrincipal(user));
    }
}