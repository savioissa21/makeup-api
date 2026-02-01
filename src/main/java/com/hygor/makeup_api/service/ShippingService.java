package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.shipping.ShippingOptionResponse;
import com.hygor.makeup_api.exception.custom.BusinessException;
import com.hygor.makeup_api.gateway.ShippingGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingService {

    private final ShippingGateway shippingGateway; // Injeção da Interface

    public ShippingOptionResponse calculateBestOption(String zipCode) {
        log.info("Solicitando cotação de frete para CEP: {}", zipCode);

        // 1. Delega o cálculo para o adaptador (seja interno, Correios ou Melhor Envio)
        List<ShippingOptionResponse> options = shippingGateway.calculateShipping(zipCode);

        if (options == null || options.isEmpty()) {
            throw new BusinessException("Nenhuma opção de frete disponível para este CEP.");
        }

        // 2. Regra de Negócio: Selecionar a opção mais barata automaticamente
        return options.stream()
                .min(Comparator.comparing(ShippingOptionResponse::getPrice))
                .orElseThrow(() -> new BusinessException("Erro ao selecionar melhor frete."));
    }
}