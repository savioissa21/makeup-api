package com.hygor.makeup_api.interceptor;

import com.hygor.makeup_api.service.RateLimitService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // 1. Identifica quem é o cliente
        String apiKey;
        boolean isAuthenticated = false;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            // Se estiver logado, a chave é o email (ex: hygor@email.com)
            apiKey = auth.getName();
            isAuthenticated = true;
        } else {
            // Se for anônimo, a chave é o IP
            apiKey = getClientIP(request);
        }

        // 2. Pega o bucket desse cliente
        Bucket tokenBucket = rateLimitService.resolveBucket(apiKey, isAuthenticated);

        // 3. Tenta consumir 1 token
        ConsumptionProbe probe = tokenBucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // SUCESSO: Adiciona headers informativos (Boa prática de API)
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true; // Deixa passar
        } else {
            // FALHA: Limite excedido
            long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
            log.warn("Rate Limit excedido para: {} (Aguarde {}s)", apiKey, waitForRefill);
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); // 429
            response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.getWriter().write("Muitas requisicoes. Tente novamente em " + waitForRefill + " segundos.");
            
            return false; // Bloqueia a requisição
        }
    }

    // Método utilitário para pegar o IP real (mesmo atrás de proxy/Cloudflare)
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}