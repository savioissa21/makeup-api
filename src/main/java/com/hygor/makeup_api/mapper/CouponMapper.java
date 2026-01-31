package com.hygor.makeup_api.mapper;

import com.hygor.makeup_api.dto.coupon.CouponResponse;
import com.hygor.makeup_api.model.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CouponMapper {

    // O MapStruct mapeia automaticamente booleanos (isValid() -> valid)
    // Se o nome fosse diferente, usaríamos: @Mapping(target = "valid", source = "valid")
    CouponResponse toResponse(Coupon coupon);
}