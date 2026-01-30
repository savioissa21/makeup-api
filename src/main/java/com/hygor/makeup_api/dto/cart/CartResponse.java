package com.hygor.makeup_api.dto.cart;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartResponse {
    private List<CartItemResponse> items;
    private BigDecimal totalAmount; // Calculado no Service para segurança
}