package com.hygor.makeup_api.repository;

import com.hygor.makeup_api.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}