package com.hygor.makeup_api.infrastructure.adapter;

import com.hygor.makeup_api.dto.shipping.ShippingOptionResponse;
import com.hygor.makeup_api.gateway.ShippingGateway;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class InternalShippingAdapter implements ShippingGateway {

    @Override
    public List<ShippingOptionResponse> calculateShipping(String zipCode) {
        // Lógica simulada que estava no Service
        boolean isLocal = zipCode != null && (zipCode.startsWith("0") || zipCode.startsWith("1"));

        ShippingOptionResponse sedex = new ShippingOptionResponse();
        sedex.setName("Correios (SEDEX)");
        sedex.setPrice(isLocal ? new BigDecimal("15.90") : new BigDecimal("32.50"));
        sedex.setDelivery_time(isLocal ? 2 : 5);

        ShippingOptionResponse pac = new ShippingOptionResponse();
        pac.setName("Correios (PAC)");
        pac.setPrice(isLocal ? new BigDecimal("10.00") : new BigDecimal("22.00"));
        pac.setDelivery_time(isLocal ? 5 : 12);

        return List.of(sedex, pac);
    }
}