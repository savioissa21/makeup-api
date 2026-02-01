package com.hygor.makeup_api.config;

import com.hygor.makeup_api.interceptor.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Aplica o Rate Limit em TUDO (/api/**)
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**"); 
                // Se quiser excluir swagger ou static resources: .excludePathPatterns("/swagger-ui/**")
    }
}