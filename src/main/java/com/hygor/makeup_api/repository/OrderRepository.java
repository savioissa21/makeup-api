package com.hygor.makeup_api.repository;

import com.hygor.makeup_api.dto.admin.TopProductDTO;
import com.hygor.makeup_api.model.Order;
import com.hygor.makeup_api.model.OrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Interface de acesso a dados para a entidade Order (Pedidos).
 */
@Repository
public interface OrderRepository extends BaseEntityRepository<Order, Long> {

    // Busca um pedido pelo seu número único de rastreio interno
    Optional<Order> findByOrderNumber(String orderNumber);

    // Lista o histórico de pedidos de um cliente específico (Área do Cliente)
    Page<Order> findByCustomerEmail(String email, Pageable pageable);

    // Usado pelo Admin para buscar pedidos por status (Financeiro/Logística)
    Page<Order> findByStatus(String status, Pageable pageable);

    Optional<Order> findByPaymentExternalId(String externalId);

    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.items i " +
            "WHERE o.customer.email = :email " +
            "AND i.product.id = :productId " +
            "AND o.status = 'DELIVERED'")
    boolean hasPurchasedProduct(@Param("email") String email, @Param("productId") Long productId);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status NOT IN (:excludedStatuses) AND o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalRevenue(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("excludedStatuses") List<OrderStatus> excludedStatuses);

    // 2. Contagem de Pedidos Válidos
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status NOT IN (:excludedStatuses) AND o.orderDate BETWEEN :startDate AND :endDate")
    Long countValidOrders(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("excludedStatuses") List<OrderStatus> excludedStatuses);

    // 3. Produtos Mais Vendidos (Complexa: Junta OrderItem, Variant e Product)
    @Query("SELECT new com.hygor.makeup_api.dto.admin.TopProductDTO(p.name, v.name, SUM(oi.quantity)) " +
            "FROM OrderItem oi " +
            "JOIN oi.variant v " +
            "JOIN v.product p " +
            "JOIN oi.order o " +
            "WHERE o.status NOT IN (:excludedStatuses) " +
            "GROUP BY p.id, v.id, p.name, v.name " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<TopProductDTO> findTopSellingProducts(@Param("excludedStatuses") List<OrderStatus> excludedStatuses,
            Pageable pageable);
}