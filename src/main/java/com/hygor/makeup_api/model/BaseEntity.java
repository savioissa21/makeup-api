package com.hygor.makeup_api.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Classe base para todas as entidades da boutique Hygor & Ana Julia.
 * Implementa auditoria automática e suporte para Soft Delete (exclusão lógica).
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Data de criação do registo - gerida automaticamente pelo Spring
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Data da última atualização - gerida automaticamente pelo Spring
    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime updatedAt;

    // Utilizador que criou o registo - integrado com o Spring Security
    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    // Utilizador que fez a última alteração
    @LastModifiedBy
    @Column(insertable = false)
    private String updatedBy;

    // Flag para Soft Delete: indica se o registo foi "removido" logicamente
    @Column(nullable = false)
    private boolean deleted = false;

    /**
     * Método para marcar o registo como removido sem o apagar fisicamente da base de dados.
     */
    public void delete() {
        this.deleted = true;
    }
}