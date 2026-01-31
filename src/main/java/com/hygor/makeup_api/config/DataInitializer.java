package com.hygor.makeup_api.config;

import com.hygor.makeup_api.model.Role;
import com.hygor.makeup_api.model.User;
import com.hygor.makeup_api.repository.RoleRepository;
import com.hygor.makeup_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Agora confiamos que o Flyway (V2) já criou as Roles.
        // Focamo-nos apenas em garantir que o Super Admin existe.
        
        if (!userRepository.existsByEmail("admin@hygoranajulia.com")) {
            
            // Busca a Role de forma segura (lança erro se o Flyway tiver falhado)
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Erro Crítico: ROLE_ADMIN não encontrada. Verifique a migração V2."));

            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("Sistema")
                    .email("admin@hygoranajulia.com")
                    .password(passwordEncoder.encode("Admin123!")) // Aqui está a vantagem de manter no Java! 🔒
                    .enabled(true)
                    .mfaEnabled(false) // Admin inicial sem MFA para não trancar acesso
                    .roles(Collections.singleton(adminRole))
                    .build();

            userRepository.save(admin);
            log.info("✅ Administrador padrão criado: admin@hygoranajulia.com / Admin123!");
        } else {
            log.info("⚡ Administrador já existe, ignorando criação.");
        }
    }
}