package com.hygor.makeup_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.util.List;

@Entity
@Table(name = "brands", indexes = {
    @Index(name = "idx_brand_slug", columnList = "slug", unique = true)
})
@SQLRestriction("deleted = false")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Brand extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String logoUrl;

    @OneToMany(mappedBy = "brand")
    private List<Product> products;
}