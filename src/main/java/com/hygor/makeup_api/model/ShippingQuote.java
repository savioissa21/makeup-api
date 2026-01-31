package com.hygor.makeup_api.model; // Ou com.hygor.makeup_api.model

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Representa uma cotação de frete interna (Desacoplada do DTO).
 * Útil para quando integrares com APIs externas (Correios, Melhor Envio).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShippingQuote {
    private String serviceName;   // Ex: "SEDEX"
    private BigDecimal cost;      // Valor calculado
    private Integer estimatedDays; // Prazo
}