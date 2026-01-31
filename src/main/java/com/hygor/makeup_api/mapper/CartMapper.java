package com.hygor.makeup_api.mapper;

import com.hygor.makeup_api.dto.cart.CartResponse;
import com.hygor.makeup_api.model.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class})
public interface CartMapper {
    @Mapping(target = "items", source = "items")
    CartResponse toResponse(Cart cart);
}