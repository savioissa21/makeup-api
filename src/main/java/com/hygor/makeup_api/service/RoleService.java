package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.role.RoleRequest;
import com.hygor.makeup_api.dto.role.RoleResponse;
import com.hygor.makeup_api.model.Role;
import com.hygor.makeup_api.repository.PermissionRepository;
import com.hygor.makeup_api.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class RoleService extends BaseService<Role, RoleRepository> {

    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository repository, PermissionRepository permissionRepository) {
        super(repository);
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        // 1. Validação de duplicidade
        if (repository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Este papel já existe no sistema.");
        }

        // 2. Padronização: garante que comece com ROLE_
        String roleName = request.getName().toUpperCase();
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }

        // 3. Busca e associa as permissões
        Role role = Role.builder()
                .name(roleName)
                .permissions(new HashSet<>(permissionRepository.findAllById(request.getPermissionIds())))
                .build();

        Role saved = repository.save(role);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public RoleResponse findByName(String name) {
        Role role = repository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Papel não encontrado: " + name));
        return mapToResponse(role);
    }

    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .permissions(role.getPermissions().stream()
                        .map(p -> p.getName())
                        .collect(Collectors.toSet()))
                .build();
    }
}