package com.hygor.makeup_api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    /**
     * 1. Configuração do Serializador JSON (Para resolver o problema das Datas)
     * Isso garante que LocalDateTime seja salvo como string (ISO-8601) e não arrays de números.
     */
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        // Configura o ObjectMapper para aceitar datas do Java 8
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // OBRIGATÓRIO para LocalDateTime
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Salva como "2023-10-25T10:00:00"

        // Cria o serializador usando esse Mapper configurado
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60)) // Default Global: 60 min
                .disableCachingNullValues()       // Não salva null no cache
                
                // Define que as CHAVES são Strings legíveis
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                // Define que os VALORES são JSON (com suporte a datas)
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );
    }

    /**
     * 2. Customizador de TTL por Nome de Cache
     * Aqui definimos as regras específicas para cada parte do sistema.
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(RedisCacheConfiguration defaultCacheConfig) {
        return (builder) -> builder
                // Define a configuração padrão
                .cacheDefaults(defaultCacheConfig)

                // --- REGRAS ESPECÍFICAS ---
                
                // Dashboard: Dados mais voláteis (10 minutos)
                // O admin quer ver as vendas recentes logo, mas não precisa ser real-time absoluto.
                .withCacheConfiguration("dashboard_stats",
                        defaultCacheConfig.entryTtl(Duration.ofMinutes(10)))

                // Detalhes do Produto: Mudam pouco (1 Hora)
                // Como temos o @CacheEvict no update, se mudar, limpa na hora.
                // Mas se ninguem mexer, segura por 1h.
                .withCacheConfiguration("product_details",
                        defaultCacheConfig.entryTtl(Duration.ofHours(1)))

                // Listagem de Produtos (Paginação): 1 Hora
                .withCacheConfiguration("products",
                        defaultCacheConfig.entryTtl(Duration.ofHours(1)))
                
                // Exemplo: Token de recuperação de senha (apenas 15 min)
                .withCacheConfiguration("password_recovery",
                         defaultCacheConfig.entryTtl(Duration.ofMinutes(15)));
    }
}