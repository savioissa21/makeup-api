package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.payment.MercadoPagoWebhookDTO;
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

    // Injeção de dependências atualizada para suportar a lógica de pedidos
    public PaymentService(PaymentRepository repository, 
                          OrderRepository orderRepository, 
                          OrderService orderService) {
        super(repository);
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    /**
     * Cria um pagamento Pix no Mercado Pago.
     * Mantém a lógica original que já funcionava perfeitamente.
     */
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
        
        // Salva o ID externo para futura conciliação via Webhook
        payment.setExternalId(resource.getId().toString());
        repository.save(payment);

        log.info("Pagamento Pix criado com sucesso. ID Externo: {}", resource.getId());
        return resource;
    }

    /**
     * Processa o Webhook recebido, valida na API oficial e atualiza o pedido.
     * Lógica blindada contra fraudes e erros de sincronização.
     */
    @Transactional
    public void processWebhook(MercadoPagoWebhookDTO webhook) {
        // 1. Filtra apenas notificações relevantes de pagamento
        if (webhook.getAction() != null && !webhook.getType().equals("payment")) {
            log.debug("Webhook ignorado: Tipo {} não é pagamento.", webhook.getType());
            return;
        }

        try {
            // 2. Extrai o ID e consulta a API do Mercado Pago (Fonte da Verdade) 🛡️
            String paymentId = webhook.getData().getId();
            log.info("Iniciando processamento do Webhook para Pagamento ID: {}", paymentId);

            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();
            
            // Busca o objeto real na API para garantir que o status é verídico
            com.mercadopago.resources.payment.Payment mpPayment = client.get(Long.valueOf(paymentId));

            // 3. Encontra o Pedido vinculado a este pagamento
            Order order = orderRepository.findByPaymentExternalId(paymentId)
                    .orElseThrow(() -> new RuntimeException("Pedido não encontrado para o pagamento ID: " + paymentId));

            // 4. Traduz o status do Mercado Pago para o nosso sistema
            PaymentStatus newStatus = mapMercadoPagoStatus(mpPayment.getStatus());

            // 5. Aciona o OrderService para gerir stock e status do pedido
            orderService.processPaymentNotification(order, newStatus);

        } catch (Exception e) {
            log.error("Erro crítico ao processar webhook do Mercado Pago: {}", e.getMessage(), e);
            // Opcional: Lançar exceção se quiser que o Mercado Pago tente reenviar (retry)
        }
    }

    /**
     * Tradutor de status: Mercado Pago -> Boutique Hygor & Ana Julia
     */
    private PaymentStatus mapMercadoPagoStatus(String mpStatus) {
        if (mpStatus == null) return PaymentStatus.PENDING;

        switch (mpStatus.toLowerCase()) {
            case "approved": 
                return PaymentStatus.APPROVED;
            case "pending":
            case "in_process":
            case "authorized": 
                return PaymentStatus.PENDING;
            case "rejected":
            case "cancelled":
            case "refunded":
            case "charged_back": 
                return PaymentStatus.CANCELLED;
            default: 
                log.warn("Status desconhecido recebido do Mercado Pago: {}", mpStatus);
                return PaymentStatus.PENDING;
        }
    }
}