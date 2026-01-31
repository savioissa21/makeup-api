package com.hygor.makeup_api.mapper;

import com.hygor.makeup_api.dto.user.UserResponse;
import com.hygor.makeup_api.model.Role;
import com.hygor.makeup_api.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface UserMapper {

    // Converte roles manualmente, mas deixa o AddressMapper lidar com os endereços automaticamente
    @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
    UserResponse toResponse(User user);

    default Set<String> mapRoles(Set<Role> roles) {
        if (roles == null) return null;
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}