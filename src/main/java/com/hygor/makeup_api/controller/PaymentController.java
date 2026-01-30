package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.model.OrderStatus;
import com.hygor.makeup_api.service.OrderService;
import com.hygor.makeup_api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService; // Injetado para gerir os pedidos

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody Map<String, Object> payload) {
        // Verifica se é uma atualização de pagamento
        if ("payment.updated".equals(payload.get("action")) || "payment.created".equals(payload.get("action"))) {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            String externalId = data.get("id").toString();

            // Atualiza o status do pedido de forma performante usando o Service
            orderService.updateOrderStatusByPaymentId(externalId, OrderStatus.PROCESSING);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/pix")
    public ResponseEntity<?> createPix(@RequestBody com.hygor.makeup_api.model.Payment payment, Authentication authentication) {
        try {
            var response = paymentService.createPixPayment(payment, authentication.getName());
            return ResponseEntity.ok(Map.of(
                "id", response.getId(),
                "qr_code", response.getPointOfInteraction().getTransactionData().getQrCode(),
                "qr_code_base64", response.getPointOfInteraction().getTransactionData().getQrCodeBase64(),
                "status", response.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao gerar Pix: " + e.getMessage());
        }
    }
}