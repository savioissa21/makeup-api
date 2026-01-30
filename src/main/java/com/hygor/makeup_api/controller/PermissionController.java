package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.dto.role.PermissionRequest;
import com.hygor.makeup_api.dto.role.PermissionResponse;
import com.hygor.makeup_api.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * Lista todas as permissões ativas no sistema.
     */
    @GetMapping
    public ResponseEntity<List<PermissionResponse>> getAll() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    /**
     * Cria uma nova permissão granular.
     */
    @PostMapping
    public ResponseEntity<PermissionResponse> create(@Valid @RequestBody PermissionRequest request) {
        return ResponseEntity.ok(permissionService.createPermission(request));
    }
}