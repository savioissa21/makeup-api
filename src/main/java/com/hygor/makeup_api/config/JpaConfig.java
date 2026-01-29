package com.hygor.makeup_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;

/**
 * Configuração para habilitar o preenchimento automático de datas e usuários.
 * Agora integrado com o Spring Security para capturar o usuário autenticado.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () => {
            // Obtém o objeto de autenticação do contexto do Spring Security
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Verifica se o usuário está logado e se não é um acesso anônimo
            if (authentication == null || 
                !authentication.isAuthenticated() || 
                authentication.getPrincipal().equals("anonymousUser")) {
                return Optional.of("SYSTEM_BOOTSTRAP");
            }

            // Retorna o e-mail/username do usuário logado para os campos @CreatedBy e @LastModifiedBy
            return Optional.ofNullable(authentication.getName());
        };
    }
}