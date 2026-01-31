package com.hygor.makeup_api.dto.admin;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private BigDecimal totalRevenue;      // Faturamento Total
    private Long totalOrders;             // Qtd Pedidos
    private BigDecimal averageTicket;     // Ticket Médio
    private Integer lowStockCount;        // Produtos em alerta
    private List<TopProductDTO> topSellingProducts; // Top 5 produtos
}