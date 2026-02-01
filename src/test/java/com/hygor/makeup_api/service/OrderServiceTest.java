package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.order.OrderItemRequest;
import com.hygor.makeup_api.dto.order.OrderRequest;
import com.hygor.makeup_api.dto.shipping.ShippingOptionResponse;
import com.hygor.makeup_api.gateway.PaymentGateway;
import com.hygor.makeup_api.mapper.OrderMapper;
import com.hygor.makeup_api.model.*;
import com.hygor.makeup_api.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private ShippingService shippingService;
    @Mock private UserRepository userRepository;
    @Mock private CartRepository cartRepository;
    @Mock private EmailService emailService;
    @Mock private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService; // O Mockito injeta os mocks acima aqui dentro

    @Test
    @DisplayName("Deve criar pedido com sucesso e baixar estoque")
    void shouldCreateOrderSuccessfully() {
        // --- 1. CENÁRIO (GIVEN) ---
        String userEmail = "teste@email.com";
        mockSecurityContext(userEmail);

        // Cliente
        User user = new User();
        user.setEmail(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        // Endereço
        Address address = new Address();
        address.setZipCode("12345-678");
        address.setUser(user);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        // Frete Mockado (Não chama Correios nem Adapter!)
        ShippingOptionResponse shipping = new ShippingOptionResponse();
        shipping.setPrice(new BigDecimal("20.00"));
        shipping.setName("Sedex");
        when(shippingService.calculateBestOption("12345-678")).thenReturn(shipping);

        // Produto/Variante (Com Lock Mockado)
        Product product = new Product();
        product.setName("Batom Vermelho");
        
        ProductVariant variant = new ProductVariant();
        variant.setId(10L);
        variant.setProduct(product);
        variant.setPrice(new BigDecimal("50.00"));
        variant.setStockQuantity(10); // Tem 10 no estoque
        
        // Importante: Simulamos o findByIdWithLock
        when(variantRepository.findByIdWithLock(10L)).thenReturn(Optional.of(variant));

        // Request do Usuário
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setVariantId(10L);
        itemReq.setQuantity(2); // Compra 2

        OrderRequest request = new OrderRequest();
        request.setAddressId(1L);
        request.setItems(List.of(itemReq));

        // Mock do salvamento
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- 2. AÇÃO (WHEN) ---
        orderService.createOrder(request);

        // --- 3. VERIFICAÇÃO (THEN) ---
        
        // Verifica se o estoque baixou de 10 para 8
        assertEquals(8, variant.getStockQuantity());
        
        // Verifica se o variantRepository.save foi chamado (para persistir o estoque novo)
        verify(variantRepository, times(1)).save(variant);
        
        // Verifica se o email de confirmação foi enviado
        verify(emailService, times(1)).sendOrderConfirmation(any());
    }

    // Método auxiliar para mockar o Usuário Logado (Security Context)
    private void mockSecurityContext(String email) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        SecurityContextHolder.setContext(securityContext);
    }
}