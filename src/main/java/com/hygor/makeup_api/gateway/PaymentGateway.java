package com.hygor.makeup_api.gateway;

import com.hygor.makeup_api.gateway.dto.PaymentGatewayRequest;
import com.hygor.makeup_api.gateway.dto.PaymentGatewayResponse;
import com.hygor.makeup_api.model.PaymentStatus;

public interface PaymentGateway {
    PaymentGatewayResponse createPix(PaymentGatewayRequest request);
    PaymentStatus getPaymentStatus(String externalId);
}