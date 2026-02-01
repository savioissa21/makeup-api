package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.auth.AuthResponse;
import com.hygor.makeup_api.dto.auth.LoginRequest;
import com.hygor.makeup_api.exception.custom.BusinessException;
import com.hygor.makeup_api.model.Role;
import com.hygor.makeup_api.model.User;
import com.hygor.makeup_api.repository.RoleRepository;
import com.hygor.makeup_api.repository.UserRepository;
import com.hygor.makeup_api.security.JwtService;
import com.hygor.makeup_api.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private MfaService mfaService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("REGISTRO: Deve registrar usuário com sucesso e retornar Token")
    void shouldRegisterUser_Successfully() {
        // --- GIVEN ---
        User user = new User();
        user.setEmail("novo@email.com");
        user.setPassword("123456");

        Role role = new Role();
        role.setName("ROLE_CUSTOMER");

        // Mocks
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_CUSTOMER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("123456")).thenReturn("senha_encriptada");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("token.jwt.valido");

        // --- WHEN ---
        String token = authService.register(user);

        // --- THEN ---
        assertNotNull(token);
        assertEquals("token.jwt.valido", token);
        
        verify(userRepository).save(user);
        verify(passwordEncoder).encode("123456");
        assertTrue(user.getRoles().contains(role));
    }

    @Test
    @DisplayName("REGISTRO: Deve lançar erro se e-mail já existe")
    void shouldThrowException_WhenEmailExists() {
        // --- GIVEN ---
        User user = new User();
        user.setEmail("existente@email.com");

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        // --- WHEN / THEN ---
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> authService.register(user));
            
        assertEquals("Este e-mail já está em uso.", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("LOGIN: Deve autenticar e retornar token")
    void shouldAuthenticate_Successfully() {
        // --- GIVEN ---
        LoginRequest request = new LoginRequest();
        request.setEmail("teste@email.com");
        request.setPassword("123456");

        User user = new User();
        user.setEmail("teste@email.com");
        user.setMfaEnabled(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("token.jwt.login");

        // --- WHEN ---
        AuthResponse response = authService.authenticate(request);

        // --- THEN ---
        assertNotNull(response.getToken());
        assertEquals("token.jwt.login", response.getToken());
        assertFalse(response.isMfaRequired());
    }

    @Test
    @DisplayName("LOGIN: Deve falhar com credenciais inválidas")
    void shouldThrowException_WhenBadCredentials() {
        // --- GIVEN ---
        // CORREÇÃO: Usando setters em vez de construtor não existente
        LoginRequest request = new LoginRequest();
        request.setEmail("errado@email.com");
        request.setPassword("senhaerrada");

        // Simula erro do Spring Security
        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        // --- WHEN / THEN ---
        assertThrows(BadCredentialsException.class, 
            () -> authService.authenticate(request));
            
        verify(userRepository, never()).findByEmail(anyString());
    }
}