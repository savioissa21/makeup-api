package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.model.*; // Importa a tua entidade Payment e Enums
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
    @Operation(summary = "Gera Pix para pedido", description = "Calcula o valor real do banco e vincula o pagamento.")
    public ResponseEntity<?> createPix(@PathVariable String orderNumber, Authentication authentication) {
        try {
            // 1. Busca a entidade real sem erro de cast ✨
            Order order = orderService.findEntityByOrderNumber(orderNumber);
            
            // 2. Cria o objeto usando os Enums agora públicos
            Payment payment = Payment.builder()
                    .amount(order.getTotalAmount())
                    .status(PaymentStatus.PENDING)
                    .method(PaymentMethod.PIX)
                    .build();

            // 3. Gera o Pix no Mercado Pago
            var response = paymentService.createPixPayment(payment, authentication.getName());
            
            // 4. Salva a relação para o Webhook funcionar 🔗
            order.setPayment(payment);
            orderService.saveOrder(order);

            return ResponseEntity.ok(Map.of(
                "order_number", orderNumber,
                "qr_code", response.getPointOfInteraction().getTransactionData().getQrCode(),
                "status", response.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro: " + e.getMessage());
        }
    }
}