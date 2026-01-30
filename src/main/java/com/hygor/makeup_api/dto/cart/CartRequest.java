package com.hygor.makeup_api.dto.cart;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

/**
 * DTO para operações que envolvem o carrinho completo.
 * Útil para sincronização inicial ou limpeza/atualização em massa.
 */
@Data
public class CartRequest {

    @NotEmpty(message = "O carrinho deve conter pelo menos um item")
    private List<CartItemRequest> items;
    
    // Podemos adicionar aqui um cupão de desconto no futuro
    // private String couponCode;
}