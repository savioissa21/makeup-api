package com.hygor.makeup_api.mapper;

import com.hygor.makeup_api.dto.role.PermissionResponse;
import com.hygor.makeup_api.model.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionResponse toResponse(Permission permission);
}