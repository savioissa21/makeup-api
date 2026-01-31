package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.admin.DashboardResponse;
import com.hygor.makeup_api.dto.admin.TopProductDTO;
import com.hygor.makeup_api.model.OrderStatus;
import com.hygor.makeup_api.repository.OrderRepository;
import com.hygor.makeup_api.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository; // Para contar stock baixo geral

    @Transactional(readOnly = true)
    public DashboardResponse getMonthlyStats() {
        // Define o período (Mês atual)
        LocalDateTime start = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        LocalDateTime end = LocalDateTime.now();

        // Status que não contam como venda
        List<OrderStatus> excluded = List.of(OrderStatus.CANCELLED, OrderStatus.WAITING_PAYMENT);

        // 1. Buscas no Repositório
        BigDecimal revenue = orderRepository.calculateTotalRevenue(start, end, excluded);
        Long ordersCount = orderRepository.countValidOrders(start, end, excluded);

        // Tratamento de null (caso não haja vendas)
        revenue = (revenue == null) ? BigDecimal.ZERO : revenue;
        ordersCount = (ordersCount == null) ? 0L : ordersCount;

        // 2. Ticket Médio
        BigDecimal avgTicket = BigDecimal.ZERO;
        if (ordersCount > 0) {
            avgTicket = revenue.divide(BigDecimal.valueOf(ordersCount), 2, RoundingMode.HALF_UP);
        }

        // 3. Top 5 Produtos
        List<TopProductDTO> topProducts = orderRepository.findTopSellingProducts(excluded, PageRequest.of(0, 5));

        // 4. Contagem de Produtos com Stock Baixo (< 5)
        // Precisas criar este método simples no ProductVariantRepository
        Integer lowStockCount = variantRepository.countByStockQuantityLessThan(5);

        return DashboardResponse.builder()
                .totalRevenue(revenue)
                .totalOrders(ordersCount)
                .averageTicket(avgTicket)
                .lowStockCount(lowStockCount)
                .topSellingProducts(topProducts)
                .build();
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardStats(LocalDate startDate, LocalDate endDate) {
        // Se não informar datas, assume o mês atual (padrão)
        LocalDateTime start = (startDate != null) ? startDate.atStartOfDay()
                : LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);

        LocalDateTime end = (endDate != null) ? endDate.atTime(23, 59, 59)
                : LocalDateTime.now();

        List<OrderStatus> excluded = List.of(OrderStatus.CANCELLED, OrderStatus.WAITING_PAYMENT);

        // Reutiliza as queries que já criamos no passo anterior
        BigDecimal revenue = orderRepository.calculateTotalRevenue(start, end, excluded);
        Long ordersCount = orderRepository.countValidOrders(start, end, excluded);

        revenue = (revenue == null) ? BigDecimal.ZERO : revenue;
        ordersCount = (ordersCount == null) ? 0L : ordersCount;

        BigDecimal avgTicket = BigDecimal.ZERO;
        if (ordersCount > 0) {
            avgTicket = revenue.divide(BigDecimal.valueOf(ordersCount), 2, RoundingMode.HALF_UP);
        }

        List<TopProductDTO> topProducts = orderRepository.findTopSellingProducts(excluded, PageRequest.of(0, 5));
        Integer lowStockCount = variantRepository.countByStockQuantityLessThan(5);

        return DashboardResponse.builder()
                .totalRevenue(revenue)
                .totalOrders(ordersCount)
                .averageTicket(avgTicket)
                .lowStockCount(lowStockCount)
                .topSellingProducts(topProducts)
                .build();
    }
}