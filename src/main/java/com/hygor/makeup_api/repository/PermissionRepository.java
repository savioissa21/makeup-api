package com.hygor.makeup_api.repository;

import com.hygor.makeup_api.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Interface de acesso a dados para permissões granulares do sistema.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * Procura uma permissão específica pelo nome.
     * @param name Nome da permissão (ex: PRODUCT_WRITE).
     * @return Um Optional com a permissão.
     */
    Optional<Permission> findByName(String name);
}