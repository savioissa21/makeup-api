package com.hygor.makeup_api.mapper;

import com.hygor.makeup_api.dto.payment.PaymentRequest;
import com.hygor.makeup_api.dto.payment.PaymentResponse;
import com.hygor.makeup_api.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse toResponse(Payment payment);

    // Mapeamentos corrigidos:
    // Removemos id, createdAt, etc. porque o Builder do Lombok não os expõe,
    // logo o MapStruct não os consegue acessar (e eles já ficam null/default naturalmente).
    
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "amount", ignore = true)      // Valor vem do Pedido
    @Mapping(target = "externalId", ignore = true)  // Gerado pelo Gateway
    Payment toEntity(PaymentRequest request);
}