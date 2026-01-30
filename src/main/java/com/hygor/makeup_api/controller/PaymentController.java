package com.hygor.makeup_api.controller;

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
            // 1. Busca a entidade real (findEntityByOrderNumber existe no seu Service)
            Order order = orderService.findEntityByOrderNumber(orderNumber);
            
            // 2. Cria o objeto de pagamento interno 🛡️
            Payment payment = Payment.builder()
                    .amount(order.getTotalAmount())
                    .status(PaymentStatus.PENDING)
                    .method(PaymentMethod.PIX)
                    .build();

            // 3. Chama o serviço do Mercado Pago 💎
            var mpResponse = paymentService.createPixPayment(payment, authentication.getName());
            
            // 4. Vincula o pagamento ao pedido e guarda (saveOrder existe no seu Service)
            order.setPayment(payment);
            orderService.saveOrder(order);

            return ResponseEntity.ok(Map.of(
                "order_number", orderNumber,
                "external_id", mpResponse.getId(),
                "qr_code", mpResponse.getPointOfInteraction().getTransactionData().getQrCode(),
                "qr_code_base64", mpResponse.getPointOfInteraction().getTransactionData().getQrCodeBase64(),
                "status", mpResponse.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao processar pagamento: " + e.getMessage());
        }
    }
}