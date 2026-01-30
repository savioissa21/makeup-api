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
private BigDecimal subtotal;      // Valor sem desconto
private BigDecimal discountAmount; // Valor subtraído
private String appliedCoupon;     // Código do cupão para feedback visual
// O totalAmount continuará a ser o valor final a pagar
private BigDecimal shippingFee;   // Valor do frete 🚚
private String shippingMethod;    // Ex: "Correios - SEDEX"
private Integer deliveryDays;     // Prazo estimado
}