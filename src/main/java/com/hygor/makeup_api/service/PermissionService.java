package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.role.PermissionRequest;
import com.hygor.makeup_api.dto.role.PermissionResponse;
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

    public PermissionService(PermissionRepository repository) {
        super(repository);
    }

    /**
     * Cria uma nova permissão com padronização para caixa alta.
     */
    @Transactional
    public PermissionResponse createPermission(PermissionRequest request) {
        String permissionName = request.getName().toUpperCase().replace(" ", "_");

        if (repository.findByName(permissionName).isPresent()) {
            throw new RuntimeException("Esta permissão já existe: " + permissionName);
        }

        Permission permission = Permission.builder()
                .name(permissionName)
                .build();

        Permission saved = repository.save(permission);
        log.info("Nova permissão criada: {}", saved.getName());
        return mapToResponse(saved);
    }

    /**
     * Busca uma permissão pelo nome.
     */
    @Transactional(readOnly = true)
    public PermissionResponse getByName(String name) {
        Permission permission = repository.findByName(name.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Permissão não encontrada: " + name));
        return mapToResponse(permission);
    }

    /**
     * Lista todas as permissões ativas.
     */
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return repository.findAllByDeletedFalse().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PermissionResponse mapToResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .build();
    }
}