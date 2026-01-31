package com.hygor.makeup_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

@Entity
@Audited
@Table(name = "product_variants")
@SQLRestriction("deleted = false")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProductVariant extends BaseEntity {

    @Column(nullable = false)
    private String name; // Ex: "Tom 01 - Nude"

    @Column(nullable = false, unique = true)
    private String sku; // Código único (ex: BAT-MAT-ND-01)

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price; // Preço pode variar por tom em marcas de luxo

    @Column(nullable = false)
    private Integer stockQuantity;

    private String imageUrl; // Foto específica desta cor/variação

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}