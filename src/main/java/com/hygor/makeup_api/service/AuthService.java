package com.hygor.makeup_api.service;

import com.hygor.makeup_api.config.JwtService;
import com.hygor.makeup_api.model.User;
import com.hygor.makeup_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Gere o login e o registo de novos utilizadores e administradores.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public String authenticate(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        var user = userRepository.findByEmail(email)
                .orElseThrow();
        return jwtService.generateToken(user);
    }
}