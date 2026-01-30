package com.hygor.makeup_api.dto.shipping;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShippingRequest {
    private String from; // CEP Origem
    private String to;   // CEP Destino
}