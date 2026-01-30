package com.hygor.makeup_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.math.BigDecimal;

/**
 * Entidade de Produto otimizada para e-commerce de luxo.
 * Utiliza BigDecimal para precisão financeira e slugs para URLs amigáveis (SEO).
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_slug", columnList = "slug", unique = true)
})
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String brand; // Novo campo adicionado

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(precision = 19, scale = 2)
    private BigDecimal discountPrice;

    @Column(nullable = false)
    private Integer stockQuantity;

    // Prompt utilizado para gerar a imagem real via IA
    private String imagePrompt;

    private Double rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}