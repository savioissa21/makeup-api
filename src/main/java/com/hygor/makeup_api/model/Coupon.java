package com.hygor.makeup_api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Coupon extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Double discountPercentage; // Ex: 10.0 para 10%

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    @Column(nullable = false)
    private boolean active = true;

    private Integer usageLimit;
    
    @Builder.Default
    private Integer usedCount = 0;

    public boolean isValid() {
        return active && 
               expirationDate.isAfter(LocalDateTime.now()) && 
               (usageLimit == null || usedCount < usageLimit);
    }
}