package com.hygor.makeup_api.controller;

import com.hygor.makeup_api.dto.admin.DashboardResponse;
import com.hygor.makeup_api.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation; // Se tiver swagger
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')") // Garante que só admin vê isso
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getMonthlyStats());
    }
}