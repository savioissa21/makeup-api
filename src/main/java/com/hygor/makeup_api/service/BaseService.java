package com.hygor.makeup_api.service;

import com.hygor.makeup_api.exception.custom.ResourceNotFoundException;
import com.hygor.makeup_api.model.BaseEntity;
import com.hygor.makeup_api.repository.BaseEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@RequiredArgsConstructor
@Slf4j // Adicionado para rastreabilidade profissional
public abstract class BaseService<T extends BaseEntity, R extends BaseEntityRepository<T, Long>> {
    
    protected final R repository;

    @Transactional(readOnly = true)
    public List<T> findAllActive() {
        log.debug("Buscando todos os registros ativos da entidade");
        return repository.findAllByDeletedFalse(); //
    }

    @Transactional(readOnly = true)
    public T findActiveById(Long id) {
        return repository.findByIdAndDeletedFalse(id) //
                .orElseThrow(() -> {
                    log.error("Registro não encontrado ou inativo com ID: {}", id);
                    return new RuntimeException("Registo não encontrado ou inactivo.");
                });
    }

    @Transactional
    public void softDelete(Long id) {
        T entity = findActiveById(id);
        entity.delete(); //
        repository.save(entity);
        log.info("Registro com ID {} marcado como deletado (soft delete)", id);
    }
    
    // Método útil para verificar existência antes de operações pesadas
    @Transactional(readOnly = true)
    public boolean existsActive(Long id) {
        return repository.existsByIdAndDeletedFalse(id); //
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Registro não encontrado ID: " + id);
        }
        repository.deleteById(id);
    }
}