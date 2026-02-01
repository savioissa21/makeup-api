package com.hygor.makeup_api.gateway.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PaymentGatewayRequest {
    private String email;
    private BigDecimal amount;
    private String description;
}