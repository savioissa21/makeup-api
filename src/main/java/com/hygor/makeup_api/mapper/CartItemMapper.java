package com.hygor.makeup_api.mapper;

import com.hygor.makeup_api.dto.cart.CartItemResponse;
import com.hygor.makeup_api.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring") // Não precisamos de 'uses = ProductVariantMapper' aqui, faremos direto
public interface CartItemMapper {

    @Mapping(target = "variantId", source = "variant.id")
    @Mapping(target = "productName", expression = "java(item.getVariant().getProduct().getName() + \" - \" + item.getVariant().getName())")
    @Mapping(target = "productImageUrl", source = "variant.imageUrl")
    @Mapping(target = "unitPrice", source = "variant.price")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(item))")
    CartItemResponse toResponse(CartItem item);

    default BigDecimal calculateSubtotal(CartItem item) {
        if (item.getVariant() == null || item.getVariant().getPrice() == null) {
            return BigDecimal.ZERO;
        }
        return item.getVariant().getPrice().multiply(new BigDecimal(item.getQuantity()));
    }
}