package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.payment.MercadoPagoWebhookDTO;
import com.hygor.makeup_api.gateway.PaymentGateway;
import com.hygor.makeup_api.gateway.dto.PaymentGatewayRequest;
import com.hygor.makeup_api.gateway.dto.PaymentGatewayResponse;
import com.hygor.makeup_api.model.Order;
import com.hygor.makeup_api.model.Payment;
import com.hygor.makeup_api.model.PaymentStatus;
import com.hygor.makeup_api.repository.OrderRepository;
import com.hygor.makeup_api.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private OrderService orderService;
    @Mock private PaymentGateway paymentGateway; // Mockamos a INTERFACE, não o MercadoPago real

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("Deve criar Pix usando o Gateway e salvar ID externo")
    void shouldCreatePixPayment_Successfully() {
        // --- GIVEN ---
        Payment payment = new Payment();
        payment.setAmount(new BigDecimal("100.00"));

        // Mock do Gateway retornando sucesso
        PaymentGatewayResponse gatewayResponse = PaymentGatewayResponse.builder()
                .externalId("MP-12345")
                .qrCode("qrcode123")
                .status("pending")
                .build();
        
        when(paymentGateway.createPix(any(PaymentGatewayRequest.class)))
                .thenReturn(gatewayResponse);

        // --- WHEN ---
        var result = paymentService.createPixPayment(payment, "cliente@email.com");

        // --- THEN ---
        assertEquals("MP-12345", result.getExternalId());
        assertEquals("MP-12345", payment.getExternalId()); // Verificamos se atualizou a entidade
        verify(paymentRepository).save(payment); // Verificamos se salvou no banco
    }

    @Test
    @DisplayName("Webhook: Deve processar notificação de pagamento aprovado")
    void shouldProcessWebhook_Approved() {
        // --- GIVEN ---
        String externalId = "MP-999";
        
        // Simula DTO do Webhook
        MercadoPagoWebhookDTO webhook = new MercadoPagoWebhookDTO();
        webhook.setAction("payment.updated");
        webhook.setType("payment");
        
        // CORREÇÃO AQUI: O nome da classe interna é DataPart
        var data = new MercadoPagoWebhookDTO.DataPart(); 
        data.setId(externalId);
        
        webhook.setData(data);

        // Simula busca do pedido
        Order order = new Order();
        when(orderRepository.findByPaymentExternalId(externalId)).thenReturn(Optional.of(order));

        // Simula consulta ao Gateway (Fonte da verdade)
        when(paymentGateway.getPaymentStatus(externalId)).thenReturn(PaymentStatus.APPROVED);

        // --- WHEN ---
        paymentService.processWebhook(webhook);

        // --- THEN ---
        // Verifica se chamou o serviço de pedido para atualizar o status
        verify(orderService).processPaymentNotification(order, PaymentStatus.APPROVED);
    }
}