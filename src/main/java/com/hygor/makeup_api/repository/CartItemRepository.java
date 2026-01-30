package com.hygor.makeup_api.repository;

import com.hygor.makeup_api.model.CartItem;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    /**
     * Remove todos os itens de um carrinho específico (usado ao esvaziar o carrinho).
     */
    void deleteByCartId(Long cartId);

    List<CartItem> findByCartId(Long cartId);
}