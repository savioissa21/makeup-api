package com.hygor.makeup_api.dto.payment;

import lombok.Data;

@Data
public class MercadoPagoWebhookDTO {
    private String action; // Ex: "payment.created", "payment.updated"
    private String api_version;
    private DataPart data; // Objeto aninhado que contém o ID
    private String date_created;
    private Long id;
    private boolean live_mode;
    private String type; // Ex: "payment"
    private String user_id;

    @Data
    public static class DataPart {
        private String id;
    }
}