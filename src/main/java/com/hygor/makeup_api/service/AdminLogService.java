package com.hygor.makeup_api.service;

import com.hygor.makeup_api.model.AdminLog;
import com.hygor.makeup_api.repository.AdminLogRepository; // Crie este repo simples (JpaRepository)
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminLogService {

    private final AdminLogRepository repository;

    @Async // Logar não deve travar a operação principal
    public void logAction(String action, String entityName, String entityId, String details) {
        String email = "SYSTEM";
        try {
            email = SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            // Caso seja um job automático
        }

        AdminLog log = AdminLog.builder()
                .adminEmail(email)
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        repository.save(log);
    }
}