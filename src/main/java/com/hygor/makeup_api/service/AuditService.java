package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.audit.PriceHistoryDTO;
import com.hygor.makeup_api.model.ProductVariant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<PriceHistoryDTO> getPriceHistory(Long variantId) {
        AuditReader reader = AuditReaderFactory.get(entityManager);

        // Busca todas as revisões onde o ID bate e houve alteração
        List<Object[]> results = reader.createQuery()
                .forRevisionsOfEntity(ProductVariant.class, false, true)
                .add(AuditEntity.id().eq(variantId))
                .getResultList();

        return results.stream()
                .map(result -> {
                    ProductVariant variant = (ProductVariant) result[0];
                    // O Envers retorna a entidade no estado daquela revisão
                    // O result[1] contém metadados da revisão (data, timestamp)
                    return new PriceHistoryDTO(
                        variant.getPrice(), 
                        variant.getUpdatedAt() // Ou data da revisão
                    );
                })
                .collect(Collectors.toList());
    }
}