package com.hygor.makeup_api.dto.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

/**
 * DTO simplificado para atualizações rápidas de inventário pelo Admin.
 */
@Data
public class ProductStockRequest {

    @NotNull(message = "A quantidade em stock é obrigatória")
    @PositiveOrZero(message = "A quantidade em stock não pode ser negativa")
    private Integer stockQuantity;
}