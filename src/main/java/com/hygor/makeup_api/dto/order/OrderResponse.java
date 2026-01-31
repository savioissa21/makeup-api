package com.hygor.makeup_api.dto.order;

import com.hygor.makeup_api.model.OrderStatus;
import com.hygor.makeup_api.model.PaymentMethod; // Importar
import com.hygor.makeup_api.model.PaymentStatus; // Importar
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private String orderNumber;
    private LocalDateTime orderDate;
    private OrderStatus status;
    
    // Detalhamento Financeiro
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    
    private String shippingMethod;
    private String trackingCode;
    
    private Long userId;
    private String userEmail;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;

    private List<OrderItemResponse> items;
}