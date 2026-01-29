package com.hygor.makeup_api.repository;

import com.hygor.makeup_api.model.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import java.util.List;
import java.util.Optional;

/**
 * Interface de repositório base genérica.
 * A anotação @NoRepositoryBean impede que o Spring crie uma instância deste repositório.
 * Serve como base para partilhar lógica comum, como o Soft Delete, entre todas as entidades.
 */
@NoRepositoryBean
public interface BaseEntityRepository<T extends BaseEntity, ID> extends JpaRepository<T, ID> {

    /**
     * Procura todos os registos que não foram marcados como eliminados.
     * @return Lista de entidades activas.
     */
    List<T> findAllByDeletedFalse();

    /**
     * Procura um registo por ID, mas apenas se ele não estiver marcado como eliminado.
     * @param id Identificador único.
     * @return Optional com a entidade se estiver activa.
     */
    Optional<T> findByIdAndDeletedFalse(ID id);

    /**
     * Verifica a existência de um registo activo por ID.
     */
    boolean existsByIdAndDeletedFalse(ID id);
}