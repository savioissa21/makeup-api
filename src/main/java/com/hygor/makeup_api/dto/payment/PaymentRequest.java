package com.hygor.makeup_api.dto.payment;

import com.hygor.makeup_api.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "O número do pedido é obrigatório")
    private String orderNumber;

    @NotNull(message = "O método de pagamento é obrigatório")
    private PaymentMethod method;

    private Integer installments; // Opcional (para cartão de crédito)
    
    // Futuramente podes adicionar aqui campos como 'cardToken', 'issuerId', etc.
}