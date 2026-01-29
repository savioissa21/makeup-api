package com.hygor.makeup_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.util.List;

/**
 * Representa as categorias de produtos (ex: Batons, Bases, Skincare).
 * Focada em SEO com o uso de slugs.
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_category_slug", columnList = "slug", unique = true)
})
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "category")
    private List<Product> products;
}