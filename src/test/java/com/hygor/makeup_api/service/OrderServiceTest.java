package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.order.OrderItemRequest;
import com.hygor.makeup_api.dto.order.OrderRequest;
import com.hygor.makeup_api.dto.shipping.ShippingOptionResponse;
import com.hygor.makeup_api.exception.custom.InsufficientStockException;
import com.hygor.makeup_api.exception.custom.ResourceNotFoundException;
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
    private OrderService orderService;

    @Test
    @DisplayName("SUCESSO: Deve criar pedido e baixar estoque")
    void shouldCreateOrderSuccessfully() {
        // --- GIVEN ---
        String userEmail = "teste@email.com";
        mockSecurityContext(userEmail);

        User user = new User();
        user.setEmail(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        Address address = new Address();
        address.setZipCode("12345-000");
        address.setUser(user); // Endereço pertence ao user
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        ShippingOptionResponse shipping = new ShippingOptionResponse();
        shipping.setPrice(BigDecimal.TEN);
        shipping.setName("PAC");
        when(shippingService.calculateBestOption(any())).thenReturn(shipping);

        Product product = new Product();
        product.setName("Batom");
        ProductVariant variant = new ProductVariant();
        variant.setId(10L);
        variant.setProduct(product);
        variant.setPrice(new BigDecimal("50.00"));
        variant.setStockQuantity(5); // Tem 5
        
        // Simula o LOCK do banco
        when(variantRepository.findByIdWithLock(10L)).thenReturn(Optional.of(variant));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        // Request
        OrderRequest request = new OrderRequest();
        request.setAddressId(1L);
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setVariantId(10L);
        itemReq.setQuantity(2); // Compra 2
        request.setItems(List.of(itemReq));

        // --- WHEN ---
        orderService.createOrder(request);

        // --- THEN ---
        assertEquals(3, variant.getStockQuantity()); // 5 - 2 = 3
        verify(variantRepository).save(variant); // Salvou estoque novo
        verify(emailService).sendOrderConfirmation(any()); // Enviou email
    }

    @Test
    @DisplayName("ERRO: Deve lançar exceção se estoque for insuficiente")
    void shouldThrowException_WhenStockIsInsufficient() {
        // --- GIVEN ---
        String userEmail = "teste@email.com";
        mockSecurityContext(userEmail);

        User user = new User();
        user.setEmail(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        Address address = new Address();
        address.setUser(user);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(shippingService.calculateBestOption(any())).thenReturn(new ShippingOptionResponse());

        Product product = new Product();
        product.setName("Batom Raro");
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setStockQuantity(1); // Só tem 1
        
        when(variantRepository.findByIdWithLock(10L)).thenReturn(Optional.of(variant));

        // Tenta comprar 5
        OrderRequest request = new OrderRequest();
        request.setAddressId(1L);
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setVariantId(10L);
        itemReq.setQuantity(5); 
        request.setItems(List.of(itemReq));

        // --- WHEN / THEN ---
        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(request));
        
        // Garante que NÃO salvou o pedido
        verify(orderRepository, never()).save(any());
    }

    private void mockSecurityContext(String email) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);
        SecurityContextHolder.setContext(securityContext);
    }
}