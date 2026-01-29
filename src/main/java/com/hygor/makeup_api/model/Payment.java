package com.hygor.makeup_api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * Entidade para controle de transações financeiras.
 */
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

    private String externalId; // ID do Mercado Pago, Stripe, etc.

    private Integer installments;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
}

enum PaymentMethod {
    CREDIT_CARD, PIX, BOLETO
}

enum PaymentStatus {
    PENDING, APPROVED, REFUNDED, CANCELLED
}