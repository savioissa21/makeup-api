package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.dto.payment.MercadoPagoWebhookDTO;
import com.hygor.makeup_api.gateway.dto.PaymentGatewayResponse; // <--- Importante: Importe o DTO do Gateway
import com.hygor.makeup_api.model.*; 
import com.hygor.makeup_api.service.OrderService;
import com.hygor.makeup_api.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
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
    private final OrderService orderService;

    @PostMapping("/pix/order/{orderNumber}")
    @Operation(summary = "Gera Pix para um pedido", description = "Busca o valor real do pedido e gera o QR Code.")
    public ResponseEntity<?> createPix(@PathVariable String orderNumber, Authentication authentication) {
        try {
            // 1. Busca a entidade real
            Order order = orderService.findEntityByOrderNumber(orderNumber);
            
            // 2. Cria o objeto de pagamento interno
            Payment payment = Payment.builder()
                    .amount(order.getTotalAmount())
                    .status(PaymentStatus.PENDING)
                    .method(PaymentMethod.PIX)
                    .build();

            // 3. Chama o serviço (que agora usa o Adapter e retorna PaymentGatewayResponse)
            PaymentGatewayResponse response = paymentService.createPixPayment(payment, authentication.getName());
            
            // 4. Vincula o pagamento ao pedido e guarda
            order.setPayment(payment);
            orderService.saveOrder(order);

            // 5. Retorna o Map usando os campos DIRETO do DTO (Sem PointOfInteraction)
            return ResponseEntity.ok(Map.of(
                "order_number", orderNumber,
                "external_id", response.getExternalId(),       // Antes: getId()
                "qr_code", response.getQrCode(),               // Antes: getPointOfInteraction()...
                "qr_code_base64", response.getQrCodeBase64(),  // Antes: getPointOfInteraction()...
                "status", response.getStatus(),
                "ticket_url", response.getTicketUrl() != null ? response.getTicketUrl() : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao processar pagamento: " + e.getMessage());
        }
    }

    @PostMapping("/webhook")
    @Operation(summary = "Recebe notificações do Mercado Pago", description = "Endpoint público para Webhooks. Processa atualizações de status de pagamento.")
    public ResponseEntity<Void> handleWebhook(@RequestBody MercadoPagoWebhookDTO webhook) {
        paymentService.processWebhook(webhook);
        return ResponseEntity.ok().build();
    }
}