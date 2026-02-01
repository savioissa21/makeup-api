package com.hygor.makeup_api.gateway;

import com.hygor.makeup_api.dto.shipping.ShippingOptionResponse;
import java.util.List;

public interface ShippingGateway {
    List<ShippingOptionResponse> calculateShipping(String zipCode);
}