package com.hygor.makeup_api.controller;

import org.springframework.security.core.Authentication;
import com.hygor.makeup_api.model.Order;
import com.hygor.makeup_api.model.OrderStatus;
import com.hygor.makeup_api.repository.OrderRepository;
import com.hygor.makeup_api.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody Map<String, Object> payload) {
        // O Mercado Pago envia o ID do pagamento no campo "data.id"
        if (payload.get("action") != null && payload.get("action").equals("payment.created") || payload.get("action").equals("payment.updated")) {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            String externalId = data.get("id").toString();

            // 1. Procura o pagamento na tua base usando o ID externo
            // Nota: Precisas de uma lógica aqui para encontrar a Order ligada a esse payment.externalId
            
            // Exemplo simplificado de lógica de atualização:
            orderRepository.findAll().stream()
                .filter(o -> o.getPayment() != null && externalId.equals(o.getPayment().getExternalId()))
                .findFirst()
                .ifPresent(order -> {
                    // 2. Se o status for aprovado no Mercado Pago, mudamos para PROCESSING
                    order.setStatus(OrderStatus.PROCESSING);
                    orderRepository.save(order);
                });
        }

        return ResponseEntity.ok().build();
    }
    // Adiciona este método dentro da classe PaymentController
    @PostMapping("/pix")
    public ResponseEntity<?> createPix(@RequestBody com.hygor.makeup_api.model.Payment payment, Authentication authentication) {
        try {
            // Chama o serviço que já criaste
            var response = paymentService.createPixPayment(payment, authentication.getName());
            
            // Retorna os dados do Pix (incluindo o QR Code em base64 e o código para copiar)
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