package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.shipping.ShippingOptionResponse;
import com.hygor.makeup_api.dto.shipping.ShippingRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;

@Service
@Slf4j
public class ShippingService {

    @Value("${melhorenvio.token}")
    private String apiToken;

    @Value("${melhorenvio.api.url}")
    private String apiUrl;

    @Value("${boutique.origin.zipcode}")
    private String originZip;

    private final RestTemplate restTemplate = new RestTemplate();

    public ShippingOptionResponse calculateBestOption(String destinationZip) {
        try {
            ShippingRequest requestBody = ShippingRequest.builder()
                    .from(originZip)
                    .to(destinationZip)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setBearerAuth(apiToken); // Token configurado no application.properties

            HttpEntity<ShippingRequest> entity = new HttpEntity<>(requestBody, headers);

            // Chamada para o endpoint de cálculo do Melhor Envio 🚀
            ResponseEntity<ShippingOptionResponse[]> response = restTemplate.exchange(
                    apiUrl + "/api/v2/me/shipment/calculate",
                    HttpMethod.POST,
                    entity,
                    ShippingOptionResponse[].class
            );

            if (response.getBody() != null && response.getBody().length > 0) {
                return response.getBody()[0]; // Retorna a primeira opção (geralmente a mais barata)
            }
        } catch (Exception e) {
            log.error("Erro ao calcular frete real: {}. Usando frete de segurança.", e.getMessage());
        }

        // Fallback: Valor fixo caso a API falhe para a Ana Julia não perder a venda 🛡️
        ShippingOptionResponse fallback = new ShippingOptionResponse();
        fallback.setName("Correios (Contingência)");
        fallback.setPrice(new BigDecimal("25.00"));
        fallback.setDelivery_time(7);
        return fallback;
    }
}