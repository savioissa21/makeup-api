package com.hygor.makeup_api.mapper;

import com.hygor.makeup_api.dto.order.OrderResponse;
import com.hygor.makeup_api.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    // CORREÇÃO: A entidade Order chama o campo de 'customer', não 'user'
    @Mapping(target = "userId", source = "customer.id")
    @Mapping(target = "userEmail", source = "customer.email")
    
    // Agora estes campos existem no DTO, então o erro desaparece
    @Mapping(target = "paymentMethod", source = "payment.method")
    @Mapping(target = "paymentStatus", source = "payment.status")
    
    @Mapping(target = "items", source = "items")
    OrderResponse toResponse(Order order);
}