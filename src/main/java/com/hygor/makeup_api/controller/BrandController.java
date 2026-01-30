package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.dto.brand.BrandRequest;
import com.hygor.makeup_api.dto.brand.BrandResponse;
import com.hygor.makeup_api.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
@Tag(name = "Marcas", description = "Gestão de marcas da boutique (ex: Ruby Rose, MAC)")
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    @Operation(summary = "Lista todas as marcas", description = "Retorna todas as marcas ativas para filtros no frontend.")
    public ResponseEntity<List<BrandResponse>> getAll() {
        return ResponseEntity.ok(brandService.findAllActive().stream()
                .map(brand -> brandService.createBrand(null)) // Aqui deve usar o seu método de mapeamento do Service
                .toList());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria uma nova marca", description = "Apenas administradores podem cadastrar novas marcas.")
    public ResponseEntity<BrandResponse> create(@Valid @RequestBody BrandRequest request) {
        return ResponseEntity.ok(brandService.createBrand(request));
    }
}