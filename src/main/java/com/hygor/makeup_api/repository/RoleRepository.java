package com.hygor.makeup_api.repository;

import com.hygor.makeup_api.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Interface de acesso a dados para a entidade Role.
 * Define as regras de acesso (ADMIN, CUSTOMER).
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Procura uma regra pelo seu nome único.
     * @param name Nome da regra (ex: ROLE_ADMIN).
     * @return Um Optional com a regra encontrada.
     */
    Optional<Role> findByName(String name);
}