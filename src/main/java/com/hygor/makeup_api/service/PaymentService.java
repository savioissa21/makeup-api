package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.payment.MercadoPagoWebhookDTO;
import com.hygor.makeup_api.exception.custom.ResourceNotFoundException;
import com.hygor.makeup_api.gateway.PaymentGateway;
import com.hygor.makeup_api.gateway.dto.PaymentGatewayRequest;
import com.hygor.makeup_api.gateway.dto.PaymentGatewayResponse;
import com.hygor.makeup_api.model.Order;
import com.hygor.makeup_api.model.Payment;
import com.hygor.makeup_api.model.PaymentStatus;
import com.hygor.makeup_api.repository.OrderRepository;
import com.hygor.makeup_api.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PaymentService extends BaseService<Payment, PaymentRepository> {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final PaymentGateway paymentGateway; // Injeção da Interface

    public PaymentService(PaymentRepository repository,
                          OrderRepository orderRepository,
                          @Lazy OrderService orderService,
                          PaymentGateway paymentGateway) {
        super(repository);
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public PaymentGatewayResponse createPixPayment(Payment payment, String userEmail) {
        // 1. Converter Domínio -> Request do Gateway
        PaymentGatewayRequest request = PaymentGatewayRequest.builder()
                .amount(payment.getAmount())
                .email(userEmail)
                .description("Compra na Boutique Hygor & Ana Julia")
                .build();

        // 2. Chamar o Gateway (Abstração)
        PaymentGatewayResponse response = paymentGateway.createPix(request);

        // 3. Atualizar e Salvar Domínio
        payment.setExternalId(response.getExternalId());
        repository.save(payment);

        log.info("Pagamento iniciado via Gateway. ID Externo: {}", response.getExternalId());
        return response;
    }

    @Transactional
    public void processWebhook(MercadoPagoWebhookDTO webhook) {
        // Validamos se é um evento de pagamento
        if (webhook.getAction() != null && !webhook.getType().equals("payment")) {
            return;
        }

        try {
            String externalId = webhook.getData().getId();
            log.info("Processando Webhook para ID: {}", externalId);

            // Busca o pedido associado
            Order order = orderRepository.findByPaymentExternalId(externalId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado para pagamento ID: " + externalId));

            // Pergunta ao Gateway qual é o status REAL atual (Fonte da verdade)
            PaymentStatus currentStatus = paymentGateway.getPaymentStatus(externalId);

            // Processa a atualização do pedido
            orderService.processPaymentNotification(order, currentStatus);

        } catch (ResourceNotFoundException e) {
            log.error("Erro de consistência no Webhook: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Erro genérico no Webhook: ", e);
        }
    }
}