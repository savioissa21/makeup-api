package com.hygor.makeup_api.repository;

import com.hygor.makeup_api.model.AdminLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {

    // Método útil para filtrar logs por administrador (quem fez a ação)
    List<AdminLog> findByAdminEmailOrderByTimestampDesc(String adminEmail);

    // Método útil para ver o histórico de um objeto específico (ex: todas as mudanças no Produto ID 10)
    List<AdminLog> findByEntityNameAndEntityIdOrderByTimestampDesc(String entityName, String entityId);
}