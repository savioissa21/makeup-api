package com.hygor.makeup_api.repository;

import com.hygor.makeup_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Interface de acesso a dados para a entidade User.
 * Fundamental para o processo de autenticação do Spring Security.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Busca um usuário pelo e-mail (usado no login)
    Optional<User> findByEmail(String email);

    // Verifica se um e-mail já está cadastrado para evitar duplicidade
    boolean existsByEmail(String email);
}