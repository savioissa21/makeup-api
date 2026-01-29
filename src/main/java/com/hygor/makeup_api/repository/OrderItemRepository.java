package com.hygor.makeup_api.repository;

import com.hygor.makeup_api.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Interface de acesso a dados para itens individuais de uma encomenda.
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Lista todos os itens que pertencem a uma determinada encomenda.
     * @param orderId Identificador da encomenda.
     * @return Lista de itens encontrados.
     */
    List<OrderItem> findByOrderId(Long orderId);
}