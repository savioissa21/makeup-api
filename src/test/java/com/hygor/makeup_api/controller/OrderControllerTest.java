package com.hygor.makeup_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hygor.makeup_api.dto.order.OrderItemRequest; // Certifique-se de importar este DTO
import com.hygor.makeup_api.dto.order.OrderRequest;
import com.hygor.makeup_api.dto.order.OrderResponse;
import com.hygor.makeup_api.mapper.PaymentMapper;
import com.hygor.makeup_api.model.OrderStatus;
import com.hygor.makeup_api.security.JwtService;
import com.hygor.makeup_api.service.OrderService;
import com.hygor.makeup_api.service.PaymentService;
import com.hygor.makeup_api.service.RateLimitService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Spring Boot 3.4+
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false) // Ignora filtros de Spring Security para focar no Controller
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- MOCKS (Dependências do Controller) ---
    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private RateLimitService rateLimitService;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private PaymentMapper paymentMapper;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @DisplayName("POST /orders - Deve criar pedido com sucesso (Status 200)")
    @WithMockUser(username = "cliente@email.com")
    void shouldCreateOrder_WhenRequestIsValid() throws Exception {

        // =================================================================================
        // 1. PREPARAÇÃO DO RATE LIMIT (Evita Erro 500 no Interceptor)
        // =================================================================================
        Bucket mockBucket = mock(Bucket.class);
        ConsumptionProbe mockProbe = mock(ConsumptionProbe.class);

        when(mockProbe.isConsumed()).thenReturn(true);
        when(mockProbe.getRemainingTokens()).thenReturn(10L);
        when(mockBucket.tryConsumeAndReturnRemaining(1)).thenReturn(mockProbe);

        // Ensina o serviço a retornar o balde mockado quando o interceptor chamar
        when(rateLimitService.resolveBucket(anyString(), anyBoolean())).thenReturn(mockBucket);

        // =================================================================================
        // 2. PREPARAÇÃO DOS DADOS DE ENTRADA (Correção do Erro 400)
        // =================================================================================
        // Cria um item válido para a lista não ficar vazia
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setVariantId(10L);
        itemRequest.setQuantity(2);

        // Monta o Request completo
        OrderRequest request = new OrderRequest();
        request.setAddressId(1L);
        request.setItems(List.of(itemRequest)); // <--- AQUI ESTAVA O PROBLEMA ANTERIOR (LISTA VAZIA)

        // =================================================================================
        // 3. PREPARAÇÃO DO MOCK DO SERVICE (Resposta esperada)
        // =================================================================================
        OrderResponse response = OrderResponse.builder()
                .orderNumber("ORD-123")
                .totalAmount(new BigDecimal("150.00"))
                .status(OrderStatus.WAITING_PAYMENT)
                .build();

        // Quando o controller chamar o service, retorna esse response
        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(response);

        // =================================================================================
        // 4. EXECUÇÃO E VALIDAÇÃO
        // =================================================================================
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))) // Converte objeto Java para JSON string

                // Validações
                .andExpect(status().isOk()) // Espera 200 OK
                .andExpect(jsonPath("$.orderNumber").value("ORD-123")) // Era $.order_number
                .andExpect(jsonPath("$.totalAmount").value(150.00)) // Era $.total_amount
                .andExpect(jsonPath("$.status").value("WAITING_PAYMENT"));
    }
}