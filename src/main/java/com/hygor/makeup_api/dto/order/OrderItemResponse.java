package com.hygor.makeup_api.dto.order;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice; // Preço unitário no momento da compra
    private BigDecimal subtotal;  // quantity * unitPrice
}