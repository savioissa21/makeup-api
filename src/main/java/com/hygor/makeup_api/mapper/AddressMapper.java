package com.hygor.makeup_api.mapper;

import com.hygor.makeup_api.dto.address.AddressResponse;
import com.hygor.makeup_api.model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    @Mapping(target = "userId", source = "user.id")
    AddressResponse toResponse(Address address);
}