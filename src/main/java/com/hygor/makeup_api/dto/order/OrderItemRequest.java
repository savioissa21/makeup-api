package com.hygor.makeup_api.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotNull(message = "O ID do produto é obrigatório")
    private Long productId;

    @Min(value = 1, message = "A quantidade mínima é 1")
    private Integer quantity;
}