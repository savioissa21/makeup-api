package com.hygor.makeup_api.repository;

import com.hygor.makeup_api.model.Cart;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends BaseEntityRepository<Cart, Long> {

    /**
     * Procura o carrinho ativo de um utilizador através do seu ID.
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * Procura o carrinho através do e-mail do utilizador (útil para o contexto de segurança).
     */
    Optional<Cart> findByUserEmail(String email);
}