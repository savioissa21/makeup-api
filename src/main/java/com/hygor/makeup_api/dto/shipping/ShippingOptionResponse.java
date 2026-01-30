package com.hygor.makeup_api.dto.shipping;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ShippingOptionResponse {
    private String name;      // Ex: "SEDEX"
    private BigDecimal price; // Valor do frete
    private Integer delivery_time; // Dias úteis
}