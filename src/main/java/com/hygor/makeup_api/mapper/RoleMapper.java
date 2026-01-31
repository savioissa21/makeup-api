package com.hygor.makeup_api.mapper;

import com.hygor.makeup_api.dto.role.RoleResponse;
import com.hygor.makeup_api.model.Permission;
import com.hygor.makeup_api.model.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    // Abordagem direta: Chama o método default abaixo via Java puro
    @Mapping(target = "permissions", expression = "java(mapPermissions(role.getPermissions()))")
    RoleResponse toResponse(Role role);

    default Set<String> mapPermissions(Set<Permission> permissions) {
        if (permissions == null) return null;
        return permissions.stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }
}