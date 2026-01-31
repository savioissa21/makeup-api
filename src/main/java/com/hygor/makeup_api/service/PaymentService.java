package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.payment.MercadoPagoWebhookDTO;
import com.hygor.makeup_api.exception.custom.ResourceNotFoundException; // Injeção
import com.hygor.makeup_api.model.Order;
import com.hygor.makeup_api.model.Payment;
import com.hygor.makeup_api.model.PaymentStatus;
import com.hygor.makeup_api.repository.OrderRepository;
import com.hygor.makeup_api.repository.PaymentRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PaymentService extends BaseService<Payment, PaymentRepository> {

    @Value("${mercado_pago.access_token}")
    private String accessToken;

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public PaymentService(PaymentRepository repository,
                          OrderRepository orderRepository,
                          OrderService orderService) {
        super(repository);
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @Transactional
    public com.mercadopago.resources.payment.Payment createPixPayment(Payment payment, String userEmail) throws Exception {
        MercadoPagoConfig.setAccessToken(accessToken);
        PaymentClient client = new PaymentClient();

        PaymentCreateRequest createRequest = PaymentCreateRequest.builder()
                .transactionAmount(payment.getAmount())
                .description("Compra na Boutique Hygor & Ana Julia")
                .paymentMethodId("pix")
                .payer(PaymentPayerRequest.builder()
                        .email(userEmail)
                        .build())
                .build();

        com.mercadopago.resources.payment.Payment resource = client.create(createRequest);
        
        payment.setExternalId(resource.getId().toString());
        repository.save(payment);

        log.info("Pagamento Pix iniciado. ID Externo: {}", resource.getId());
        return resource;
    }

    @Transactional
    public void processWebhook(MercadoPagoWebhookDTO webhook) {
        if (webhook.getAction() != null && !webhook.getType().equals("payment")) {
            return;
        }

        try {
            String paymentId = webhook.getData().getId();
            log.info("Processando Webhook Mercado Pago: {}", paymentId);

            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();
            
            // Valida na fonte
            com.mercadopago.resources.payment.Payment mpPayment = client.get(Long.valueOf(paymentId));

            // Busca o pedido com Exceção correta 404
            Order order = orderRepository.findByPaymentExternalId(paymentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado para o pagamento ID: " + paymentId));

            PaymentStatus newStatus = mapMercadoPagoStatus(mpPayment.getStatus());
            orderService.processPaymentNotification(order, newStatus);

        } catch (ResourceNotFoundException e) {
            log.error("Erro de consistência: {}", e.getMessage());
            // Aqui não relançamos para não travar o webhook do MP
        } catch (Exception e) {
            log.error("Erro ao processar webhook: {}", e.getMessage(), e);
        }
    }

    private PaymentStatus mapMercadoPagoStatus(String mpStatus) {
        if (mpStatus == null) return PaymentStatus.PENDING;

        switch (mpStatus.toLowerCase()) {
            case "approved": return PaymentStatus.APPROVED;
            case "pending":
            case "in_process":
            case "authorized": return PaymentStatus.PENDING;
            case "rejected":
            case "cancelled":
            case "refunded":
            case "charged_back": return PaymentStatus.CANCELLED; // Agora já mapeia CHARGED_BACK corretamente
            default: 
                log.warn("Status desconhecido: {}", mpStatus);
                return PaymentStatus.PENDING;
        }
    }
}