package com.hygor.makeup_api.mapper;

import com.hygor.makeup_api.dto.order.OrderItemResponse;
import com.hygor.makeup_api.model.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "variantId", source = "variant.id")
    // Pega o nome do produto através da variante
    @Mapping(target = "productName", expression = "java(item.getVariant().getProduct().getName() + \" - \" + item.getVariant().getName())")
    @Mapping(target = "image", source = "variant.imageUrl")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(item))")
    OrderItemResponse toResponse(OrderItem item);

    default BigDecimal calculateSubtotal(OrderItem item) {
        if (item.getUnitPrice() == null) return BigDecimal.ZERO;
        return item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
    }
}