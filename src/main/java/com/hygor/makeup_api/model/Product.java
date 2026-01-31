package com.hygor.makeup_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_slug", columnList = "slug", unique = true)
})
@SQLRestriction("deleted = false")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(precision = 19, scale = 2)
    private BigDecimal discountPrice;

    // REMOVIDO: stockQuantity como campo persistido 
    // Agora o stock é gerido pelas variantes (SKUs)

    private String imageUrl;
    private Integer maxInstallments;
    private String imagePrompt;
    private Double rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    // NOVO: Relacionamento com as Variações (Cores/Tons) 💎
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    /**
     * CÁLCULO DE STOCK TOTAL: Soma dinâmica de todas as variantes.
     * Isto garante que o dado nunca esteja divergente. ✨
     */
    public Integer getTotalStockQuantity() {
        if (variants == null || variants.isEmpty()) {
            return 0;
        }
        return variants.stream()
                .mapToInt(ProductVariant::getStockQuantity)
                .sum();
    }
}