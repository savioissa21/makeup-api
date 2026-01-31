package com.hygor.makeup_api.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_logs")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AdminLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String adminEmail; // Quem fez
    private String action;     // UPDATE, DELETE, CREATE
    private String entityName; // PRODUCT, USER
    private String entityId;   // ID do objeto afetado
    private String details;    // Ex: "Mudou stock de 10 para 50"
    
    private LocalDateTime timestamp;
}