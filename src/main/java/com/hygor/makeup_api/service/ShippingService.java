package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.shipping.ShippingOptionResponse;
import com.hygor.makeup_api.mapper.ShippingMapper; // Injeção
import com.hygor.makeup_api.model.ShippingQuote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingService {

    private final ShippingMapper shippingMapper;

    /**
     * Calcula a melhor opção de frete.
     * Futuramente aqui chamarás a API dos Correios.
     */
    public ShippingOptionResponse calculateBestOption(String zipCode) {
        log.info("Calculando frete para o CEP: {}", zipCode);

        // Lógica de Negócio (Mockada por enquanto, mas isolada no objeto de domínio)
        ShippingQuote quote = calculateInternalQuote(zipCode);

        // O Mapper converte para o DTO de resposta
        return shippingMapper.toResponse(quote);
    }

    /**
     * Simula uma lógica de cálculo interna ou chamada externa.
     */
    private ShippingQuote calculateInternalQuote(String zipCode) {
        // Simulação: Se for CEP de SP (começa com 0 ou 1), é mais barato
        boolean isLocal = zipCode.startsWith("0") || zipCode.startsWith("1");
        
        return ShippingQuote.builder()
                .serviceName("Correios (SEDEX)")
                .cost(isLocal ? new BigDecimal("15.90") : new BigDecimal("32.50"))
                .estimatedDays(isLocal ? 2 : 5)
                .build();
    }
}