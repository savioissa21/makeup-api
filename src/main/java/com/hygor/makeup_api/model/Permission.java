package com.hygor.makeup_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

/**
 * Representa uma permissão específica no sistema (ex: 'PRODUCT_EDIT', 'USER_DELETE').
 */
@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private String description;
}