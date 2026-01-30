package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.dto.role.RoleRequest;
import com.hygor.makeup_api.dto.role.RoleResponse;
import com.hygor.makeup_api.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * Cria um novo papel no sistema associando permissões granulares.
     */
    @PostMapping
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(roleService.createRole(request));
    }

    /**
     * Procura um papel específico pelo nome (ex: ROLE_ADMIN).
     */
    @GetMapping("/{name}")
    public ResponseEntity<RoleResponse> getByName(@PathVariable String name) {
        return ResponseEntity.ok(roleService.findByName(name));
    }
}