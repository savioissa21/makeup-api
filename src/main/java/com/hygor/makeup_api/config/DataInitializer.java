package com.hygor.makeup_api.config;

import com.hygor.makeup_api.model.Role;
import com.hygor.makeup_api.model.User;
import com.hygor.makeup_api.repository.RoleRepository;
import com.hygor.makeup_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 1. Criar as Roles básicas se não existirem
        createRoleIfNotFound("ROLE_ADMIN");
        createRoleIfNotFound("ROLE_CUSTOMER");

        // 2. Criar o Administrador padrão se não existir
        if (!userRepository.existsByEmail("admin@hygoranajulia.com")) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").get();
            
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("Boutique")
                    .email("admin@hygoranajulia.com")
                    .password(passwordEncoder.encode("Admin123!")) // Senha forte inicial
                    .enabled(true)
                    .roles(Collections.singleton(adminRole))
                    .build();

            userRepository.save(admin);
            System.out.println("✅ Administrador padrão criado com sucesso!");
        }
    }

    private void createRoleIfNotFound(String name) {
        if (roleRepository.findByName(name).isEmpty()) {
            roleRepository.save(Role.builder().name(name).build());
        }
    }
}