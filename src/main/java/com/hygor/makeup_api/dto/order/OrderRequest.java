package com.hygor.makeup_api.dto.order;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    @NotEmpty(message = "O pedido deve ter pelo menos um item")
    private List<OrderItemRequest> items;
    
    // Podemos adicionar futuramente o ID do endereço de entrega aqui
    // private Long addressId;
}