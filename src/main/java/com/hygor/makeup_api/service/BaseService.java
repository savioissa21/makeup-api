package com.hygor.makeup_api.service;

import com.hygor.makeup_api.model.BaseEntity;
import com.hygor.makeup_api.repository.BaseEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@RequiredArgsConstructor
public abstract class BaseService<T extends BaseEntity, R extends BaseEntityRepository<T, Long>> {
    protected final R repository;

    @Transactional(readOnly = true)
    public List<T> findAllActive() {
        return repository.findAllByDeletedFalse();
    }

    @Transactional(readOnly = true)
    public T findActiveById(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Registo não encontrado ou inactivo."));
    }

    @Transactional
    public void softDelete(Long id) {
        T entity = findActiveById(id);
        entity.delete();
        repository.save(entity);
    }
}