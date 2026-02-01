package com.hygor.makeup_api.infrastructure.adapter;

import com.hygor.makeup_api.exception.custom.BusinessException;
import com.hygor.makeup_api.gateway.PaymentGateway;
import com.hygor.makeup_api.gateway.dto.PaymentGatewayRequest;
import com.hygor.makeup_api.gateway.dto.PaymentGatewayResponse;
import com.hygor.makeup_api.model.PaymentStatus;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.resources.payment.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MercadoPagoAdapter implements PaymentGateway {

    @Value("${mercado_pago.access_token}")
    private String accessToken;

    @Override
    public PaymentGatewayResponse createPix(PaymentGatewayRequest request) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();

            PaymentCreateRequest createRequest = PaymentCreateRequest.builder()
                    .transactionAmount(request.getAmount())
                    .description(request.getDescription())
                    .paymentMethodId("pix")
                    .payer(PaymentPayerRequest.builder()
                            .email(request.getEmail())
                            .build())
                    .build();

            Payment payment = client.create(createRequest);

            return PaymentGatewayResponse.builder()
                    .externalId(payment.getId().toString())
                    .status(payment.getStatus())
                    .qrCode(payment.getPointOfInteraction().getTransactionData().getQrCode())
                    .qrCodeBase64(payment.getPointOfInteraction().getTransactionData().getQrCodeBase64())
                    .ticketUrl(payment.getPointOfInteraction().getTransactionData().getTicketUrl())
                    .build();

        } catch (Exception e) {
            log.error("Erro no Mercado Pago: ", e);
            throw new BusinessException("Erro ao criar pagamento no provedor externo.");
        }
    }

    @Override
    public PaymentStatus getPaymentStatus(String externalId) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();
            Payment payment = client.get(Long.valueOf(externalId));
            return mapStatus(payment.getStatus());
        } catch (Exception e) {
            log.error("Erro ao buscar status no Mercado Pago: ", e);
            // Em caso de erro, assume pendente para não cancelar indevidamente
            return PaymentStatus.PENDING;
        }
    }

    private PaymentStatus mapStatus(String mpStatus) {
        if (mpStatus == null) return PaymentStatus.PENDING;
        switch (mpStatus.toLowerCase()) {
            case "approved": return PaymentStatus.APPROVED;
            case "pending":
            case "in_process":
            case "authorized": return PaymentStatus.PENDING;
            case "rejected":
            case "cancelled":
            case "refunded":
            case "charged_back": return PaymentStatus.CANCELLED;
            default: return PaymentStatus.PENDING;
        }
    }
}