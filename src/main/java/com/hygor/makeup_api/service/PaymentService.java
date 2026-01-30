package com.hygor.makeup_api.service;

import com.hygor.makeup_api.model.Payment; // A tua entidade do projeto
import com.hygor.makeup_api.repository.PaymentRepository; //
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService extends BaseService<Payment, PaymentRepository> {

    @Value("${mercado_pago.access_token}")
    private String accessToken;

    public PaymentService(PaymentRepository repository) {
        super(repository);
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

        // Alterado de PaymentResource para o caminho completo da classe do SDK
        com.mercadopago.resources.payment.Payment resource = client.create(createRequest);
        
        // Salva o ID externo retornado pelo Mercado Pago para conciliação
        payment.setExternalId(resource.getId().toString());
        repository.save(payment);

        return resource;
    }
}