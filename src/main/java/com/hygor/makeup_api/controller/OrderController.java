package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.dto.order.OrderRequest;
import com.hygor.makeup_api.dto.order.OrderResponse;
import com.hygor.makeup_api.dto.payment.MercadoPagoWebhookDTO;
import com.hygor.makeup_api.dto.payment.PaymentRequest;
import com.hygor.makeup_api.mapper.PaymentMapper;
import com.hygor.makeup_api.model.Payment;
import com.hygor.makeup_api.service.OrderService;
import com.hygor.makeup_api.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getMyOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.getMyOrders(pageable));
    }

    @GetMapping("/{orderNumber}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getByOrderNumber(orderNumber));
    }

    @PutMapping("/{orderNumber}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.cancelOrder(orderNumber));
    }

    // --- PAGAMENTOS & WEBHOOKS ---

    @PostMapping("/pay")
    public ResponseEntity<?> generatePayment(@RequestBody @Valid PaymentRequest request) {
        // 1. Converte DTO -> Entidade (Amount e Status são ignorados pelo Mapper por segurança)
        Payment payment = paymentMapper.toEntity(request);
        
        // 2. Busca o pedido para pegar o valor real
        var order = orderService.findEntityByOrderNumber(request.getOrderNumber());
        payment.setAmount(order.getTotalAmount());

        try {
            // 3. Gera Pix (Por enquanto só Pix, mas pronto para expansão)
            var mpPayment = paymentService.createPixPayment(payment, order.getCustomer().getEmail());
            
            // 4. Atualiza o pedido com o pagamento criado
            order.setPayment(payment);
            orderService.saveOrder(order);

            return ResponseEntity.ok(Map.of(
                "qr_code", mpPayment.getPointOfInteraction().getTransactionData().getQrCode(),
                "qr_code_base64", mpPayment.getPointOfInteraction().getTransactionData().getQrCodeBase64(),
                "ticket_url", mpPayment.getPointOfInteraction().getTransactionData().getTicketUrl()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao gerar pagamento: " + e.getMessage());
        }
    }

    @PostMapping("/webhook/payment")
    public ResponseEntity<Void> handlePaymentWebhook(@RequestBody MercadoPagoWebhookDTO webhook) {
        paymentService.processWebhook(webhook);
        return ResponseEntity.ok().build();
    }
}