package com.hygor.makeup_api.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemRequest {
    @NotNull(message = "O ID da variação (cor/tom) é obrigatório")
    private Long variantId; // Alterado de productId para variantId

    @Min(value = 1, message = "A quantidade mínima para o carrinho é 1")
    private Integer quantity;
}