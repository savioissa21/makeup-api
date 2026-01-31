package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.role.RoleRequest;
import com.hygor.makeup_api.dto.role.RoleResponse;
import com.hygor.makeup_api.exception.custom.BusinessException;
import com.hygor.makeup_api.exception.custom.ResourceNotFoundException;
import com.hygor.makeup_api.mapper.RoleMapper; // Injeção
import com.hygor.makeup_api.model.Role;
import com.hygor.makeup_api.repository.PermissionRepository;
import com.hygor.makeup_api.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
@Slf4j
public class RoleService extends BaseService<Role, RoleRepository> {

    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper; // Injeção

    public RoleService(RoleRepository repository, 
                       PermissionRepository permissionRepository,
                       RoleMapper roleMapper) {
        super(repository);
        this.permissionRepository = permissionRepository;
        this.roleMapper = roleMapper;
    }

    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        // Validação
        if (repository.findByName(request.getName()).isPresent()) {
            throw new BusinessException("Este papel já existe no sistema: " + request.getName());
        }

        // Padronização (ROLE_...)
        String roleName = request.getName().toUpperCase();
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }

        Role role = Role.builder()
                .name(roleName)
                .permissions(new HashSet<>(permissionRepository.findAllById(request.getPermissionIds())))
                .build();

        Role saved = repository.save(role);
        log.info("Novo papel criado: {}", saved.getName());
        
        return roleMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public RoleResponse findByName(String name) {
        return repository.findByName(name)
                .map(roleMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Papel não encontrado: " + name));
    }
}