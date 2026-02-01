package com.hygor.makeup_api.gateway.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentGatewayResponse {
    private String externalId;
    private String status;
    private String qrCode;
    private String qrCodeBase64;
    private String ticketUrl;
}