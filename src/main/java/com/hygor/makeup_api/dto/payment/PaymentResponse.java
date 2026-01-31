package com.hygor.makeup_api.dto.payment;

import com.hygor.makeup_api.model.PaymentMethod;
import com.hygor.makeup_api.model.PaymentStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private PaymentMethod method;
    private PaymentStatus status;
    private BigDecimal amount;
    private Integer installments;
    private String externalId; // ID do Mercado Pago (útil para auditoria)
    private LocalDateTime createdAt;
}