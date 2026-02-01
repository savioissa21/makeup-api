package com.hygor.makeup_api.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    // Cache em memória para guardar os buckets dos usuários/IPs
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Resolve o bucket baseado na chave (IP ou Email).
     * Se não existir, cria um novo com base no tipo de usuário.
     */
    public Bucket resolveBucket(String key, boolean isAuthenticated) {
        return cache.computeIfAbsent(key, k -> newBucket(isAuthenticated));
    }

    private Bucket newBucket(boolean isAuthenticated) {
        Bandwidth limit;

        if (isAuthenticated) {
            // USUÁRIO LOGADO (VIP): 100 requisições por minuto
            limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
        } else {
            // ANÔNIMO (Público): 20 requisições por minuto (Anti-spam)
            limit = Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1)));
        }

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
    
    // Opcional: Método para limpar cache antigo (pode ser agendado com @Scheduled)
    public void clearCache() {
        cache.clear();
    }
}