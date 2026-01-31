package com.hygor.makeup_api.mapper;

import com.hygor.makeup_api.dto.shipping.ShippingOptionResponse;
import com.hygor.makeup_api.model.ShippingQuote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShippingMapper {

    @Mapping(target = "name", source = "serviceName")
    @Mapping(target = "price", source = "cost")
    @Mapping(target = "delivery_time", source = "estimatedDays") // Mantendo o snake_case do teu DTO
    ShippingOptionResponse toResponse(ShippingQuote quote);
}