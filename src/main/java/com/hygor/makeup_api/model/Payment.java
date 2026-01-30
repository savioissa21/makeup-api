package com.hygor.makeup_api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String externalId; // ID retornado pelo Mercado Pago

    private Integer installments;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
}

// Enums públicos para evitar erros de visibilidade 🕵️‍♀️ ✨
public enum PaymentMethod {
    CREDIT_CARD, PIX, BOLETO
}

public enum PaymentStatus {
    PENDING, APPROVED, REFUNDED, CANCELLED
}