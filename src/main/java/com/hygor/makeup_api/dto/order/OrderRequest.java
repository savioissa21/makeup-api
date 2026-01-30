package com.hygor.makeup_api.dto.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    @NotEmpty(message = "O pedido deve ter pelo menos um item")
    private List<OrderItemRequest> items;
    // No OrderRequest.java
    @NotNull(message = "O endereço de entrega é obrigatório")
    private Long addressId;

    // Podemos adicionar futuramente o ID do endereço de entrega aqui
    // private Long addressId;
}