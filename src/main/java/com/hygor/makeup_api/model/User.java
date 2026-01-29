package com.hygor.makeup_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.Collection;
import java.util.HashSet;

/**
 * Entidade central de usuário com suporte a MFA e RBAC.
 * Utiliza SQLRestriction para garantir que usuários deletados não sejam listados.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    private String password;

    private boolean enabled = true;

    // Campos para Multi-Factor Authentication (MFA)
    private boolean mfaEnabled = false;

    private String secretMfa;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Collection<Role> roles = new HashSet<>();

    /**
     * Retorna o nome completo do usuário.
     */
    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }
}