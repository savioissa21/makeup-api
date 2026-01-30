package com.hygor.makeup_api.dto.cart;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CartItemResponse {
    private Long variantId;       // Alterado de productId para variantId
    private String productName;   // Nome do produto + Nome da variação
    private String productImageUrl; // URL da imagem específica da cor/tom
    private Integer quantity;
    private BigDecimal unitPrice; // Preço da variante específica
    private BigDecimal subtotal;
}