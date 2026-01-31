package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.cart.CartItemRequest;
import com.hygor.makeup_api.dto.cart.CartResponse;
import com.hygor.makeup_api.exception.custom.BusinessException;
import com.hygor.makeup_api.exception.custom.ResourceNotFoundException;
import com.hygor.makeup_api.mapper.CartMapper; // Injeção do Mapper
import com.hygor.makeup_api.model.*;
import com.hygor.makeup_api.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@Service
@Slf4j
public class CartService extends BaseService<Cart, CartRepository> {

    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final CouponRepository couponRepository;
    private final CartMapper cartMapper; // Mapper Injetado

    @Value("${boutique.shipping.free-threshold:200.00}")
    private BigDecimal freeShippingThreshold;

    public CartService(CartRepository repository,
                       ProductVariantRepository variantRepository,
                       UserRepository userRepository,
                       CartItemRepository cartItemRepository,
                       CouponRepository couponRepository,
                       CartMapper cartMapper) {
        super(repository);
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
        this.couponRepository = couponRepository;
        this.cartMapper = cartMapper;
    }

    @Transactional
    public CartResponse addItemToCart(CartItemRequest request) {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);

        // 1. Busca variante
        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException("Variação de produto não encontrada: " + request.getVariantId()));

        // 2. Valida stock
        if (variant.getStockQuantity() < request.getQuantity()) {
            throw new BusinessException("Stock insuficiente para a variante: " + variant.getName());
        }

        // 3. Atualiza ou Cria Item
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getVariant().getId().equals(variant.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
        }

        repository.save(cart);
        log.info("Item adicionado ao carrinho de: {}", user.getEmail());

        return buildCartResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getMyCart() {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);
        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse applyCoupon(String code) {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);

        Coupon coupon = couponRepository.findByCodeIgnoreCaseAndDeletedFalse(code)
                .orElseThrow(() -> new ResourceNotFoundException("Cupão inválido: " + code));

        if (!coupon.isValid()) {
            throw new BusinessException("O cupão expirou ou atingiu o limite de uso.");
        }

        cart.setCoupon(coupon);
        repository.save(cart);
        
        log.info("Cupão {} aplicado com sucesso.", code);
        return buildCartResponse(cart);
    }

    @Transactional
    public void clearCart() {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);
        
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cart.setCoupon(null);
        
        repository.save(cart);
        log.info("Carrinho limpo para: {}", user.getEmail());
    }

    // --- Métodos Auxiliares ---

    private Cart getOrCreateCart(User user) {
        return repository.findByUserEmail(user.getEmail())
                .orElseGet(() -> repository.save(Cart.builder().user(user).items(new ArrayList<>()).build()));
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilizador não encontrado no contexto de segurança."));
    }

    /**
     * Constrói a resposta final calculando totais e frete.
     * Usamos o cartMapper para os itens, mas calculamos os totais aqui
     * para garantir a lógica de negócio (Frete Grátis, etc).
     */
    private CartResponse buildCartResponse(Cart cart) {
        // Usa o Mapper para converter a estrutura básica (Items e Entidade Cart)
        CartResponse response = cartMapper.toResponse(cart);

        // Recalcula totais para garantir precisão (lógica de negócio)
        BigDecimal subtotal = calculateSubtotal(cart);
        BigDecimal discount = calculateDiscount(cart, subtotal);
        BigDecimal shippingFee = calculateShipping(subtotal);
        BigDecimal total = subtotal.subtract(discount).add(shippingFee);

        // Atualiza o DTO com os valores calculados
        response.setSubtotal(subtotal);
        response.setDiscountAmount(discount);
        response.setShippingFee(shippingFee);
        response.setTotalAmount(total);
        
        // Define mensagem de frete
        if (shippingFee.compareTo(BigDecimal.ZERO) == 0 && subtotal.compareTo(BigDecimal.ZERO) > 0) {
            response.setShippingMethod("Correios (Grátis) ✨");
        } else {
            response.setShippingMethod("Correios (PAC/SEDEX)");
        }
        
        // Garante que o código do cupão está na resposta
        if (cart.getCoupon() != null) {
            response.setAppliedCoupon(cart.getCoupon().getCode());
        }

        return response;
    }

    private BigDecimal calculateSubtotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> item.getVariant().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateDiscount(Cart cart, BigDecimal subtotal) {
        if (cart.getCoupon() != null && cart.getCoupon().isValid()) {
            BigDecimal percentage = BigDecimal.valueOf(cart.getCoupon().getDiscountPercentage()).divide(BigDecimal.valueOf(100));
            return subtotal.multiply(percentage);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateShipping(BigDecimal subtotal) {
        if (subtotal.compareTo(freeShippingThreshold) >= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal("25.00"); // Frete Fixo
    }
}