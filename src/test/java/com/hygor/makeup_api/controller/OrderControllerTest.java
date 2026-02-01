package com.hygor.makeup_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hygor.makeup_api.dto.order.OrderRequest;
import com.hygor.makeup_api.dto.order.OrderResponse;
import com.hygor.makeup_api.mapper.PaymentMapper;
import com.hygor.makeup_api.model.OrderStatus; // <--- IMPORTANTE: Importar o Enum
import com.hygor.makeup_api.security.JwtService;
import com.hygor.makeup_api.service.OrderService;
import com.hygor.makeup_api.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser; // <--- Agora vai funcionar
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private OrderService orderService;
    @MockBean private PaymentService paymentService;
    @MockBean private PaymentMapper paymentMapper;
    @MockBean private JwtService jwtService;

    @Test
    @DisplayName("POST /orders - Deve criar pedido com sucesso (Status 200)")
    @WithMockUser(username = "cliente@email.com")
    void shouldCreateOrder_WhenRequestIsValid() throws Exception {
        // --- GIVEN ---
        // Usando construtor padrão (se tiver @Data ou @NoArgsConstructor)
        // Se der erro aqui, mude para OrderRequest.builder()...build()
        OrderRequest request = new OrderRequest();
        request.setAddressId(1L);
        request.setItems(List.of());

        // CORREÇÃO: Usando Builder e Enum correto
        OrderResponse response = OrderResponse.builder()
                .orderNumber("ORD-123")
                .totalAmount(new BigDecimal("150.00"))
                .status(OrderStatus.WAITING_PAYMENT) // <--- Passando o ENUM, não String
                .build();

        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(response);

        // --- WHEN / THEN ---
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                
                // Validações
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order_number").value("ORD-123"))
                .andExpect(jsonPath("$.total_amount").value(150.00))
                .andExpect(jsonPath("$.status").value("WAITING_PAYMENT")); // O JSON retorna String, aqui tudo bem
    }
}