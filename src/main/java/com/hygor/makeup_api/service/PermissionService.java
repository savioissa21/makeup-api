package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.role.PermissionRequest;
import com.hygor.makeup_api.dto.role.PermissionResponse;
import com.hygor.makeup_api.exception.custom.BusinessException;
import com.hygor.makeup_api.exception.custom.ResourceNotFoundException;
import com.hygor.makeup_api.mapper.PermissionMapper; // Injeção
import com.hygor.makeup_api.model.Permission;
import com.hygor.makeup_api.repository.PermissionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PermissionService extends BaseService<Permission, PermissionRepository> {

    private final PermissionMapper permissionMapper;

    public PermissionService(PermissionRepository repository, PermissionMapper permissionMapper) {
        super(repository);
        this.permissionMapper = permissionMapper;
    }

    @Transactional
    public PermissionResponse createPermission(PermissionRequest request) {
        String permissionName = request.getName().toUpperCase().replace(" ", "_");

        if (repository.findByName(permissionName).isPresent()) {
            throw new BusinessException("Esta permissão já existe: " + permissionName);
        }

        Permission permission = Permission.builder()
                .name(permissionName)
                // Se o DTO tiver description, podes adicionar: .description(request.getDescription())
                .build();

        Permission saved = repository.save(permission);
        log.info("Nova permissão criada: {}", saved.getName());
        
        return permissionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PermissionResponse getByName(String name) {
        return repository.findByName(name.toUpperCase())
                .map(permissionMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Permissão não encontrada: " + name));
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return repository.findAllByDeletedFalse().stream()
                .map(permissionMapper::toResponse)
                .collect(Collectors.toList());
    }
}