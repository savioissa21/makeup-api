package com.hygor.makeup_api.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Collection;
import java.util.HashSet;

/**
 * Define o papel do usuário (ADMIN, CUSTOMER, etc) e agrupa permissões.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Collection<Permission> permissions = new HashSet<>();
}