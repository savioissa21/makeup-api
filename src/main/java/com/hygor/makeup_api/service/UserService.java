package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.auth.ChangePasswordRequest;
import com.hygor.makeup_api.dto.user.UserResponse;
import com.hygor.makeup_api.model.User;
import com.hygor.makeup_api.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService extends BaseService<User, UserRepository> {

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        super(repository);
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retorna o perfil do utilizador atualmente logado.
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        User currentUser = getAuthenticatedUser();
        return mapToResponse(currentUser);
    }

    /**
     * Altera a senha do utilizador logado com validação da senha antiga.
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getAuthenticatedUser();

        // 1. Verifica se a senha antiga está correta
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("A senha atual está incorreta.");
        }

        // 2. Criptografa e guarda a nova senha
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
        log.info("Senha alterada com sucesso para o utilizador: {}", user.getEmail());
    }

    /**
     * Método auxiliar para obter o utilizador do contexto de segurança.
     */
    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado no sistema."));
    }

    /**
     * Converte a Entidade User para UserResponse DTO.
     */
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(Collectors.toSet()))
                .build();
    }
}