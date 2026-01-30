package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.cart.CartItemRequest;
import com.hygor.makeup_api.dto.cart.CartResponse;
import com.hygor.makeup_api.dto.cart.CartItemResponse;
import com.hygor.makeup_api.model.*;
import com.hygor.makeup_api.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartService extends BaseService<Cart, CartRepository> {

    // Alterado de ProductRepository para ProductVariantRepository
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final CouponRepository couponRepository;

    @Value("${boutique.shipping.free-threshold:200.00}")
    private BigDecimal freeShippingThreshold;

    public CartService(CartRepository repository, 
                       ProductVariantRepository variantRepository, 
                       UserRepository userRepository, 
                       CartItemRepository cartItemRepository,
                       CouponRepository couponRepository) {
        super(repository);
        this.variantRepository = variantRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
        this.couponRepository = couponRepository;
    }

    @Transactional
    public CartResponse addItemToCart(CartItemRequest request) {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);
        
        // 1. Busca a Variante específica (SKU) em vez do produto genérico
        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new RuntimeException("Variação de produto não encontrada"));

        // 2. Validação de estoque baseada na variante escolhida
        if (variant.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Quantidade solicitada indisponível para esta cor/tom");
        }

        // 3. Filtra por variante no carrinho para evitar duplicados
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getVariant().getId().equals(variant.getId()))
                .findFirst()
                .orElse(null);

        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            cartItem = CartItem.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(cartItem);
        }

        repository.save(cart);
        log.info("Variação {} adicionada ao carrinho de {}", variant.getName(), user.getEmail());
        
        return mapToResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getMyCart() {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);
        return mapToResponse(cart);
    }

    @Transactional
    public CartResponse applyCoupon(String code) {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);
        
        Coupon coupon = couponRepository.findByCodeIgnoreCaseAndDeletedFalse(code)
                .orElseThrow(() -> new RuntimeException("Cupão inválido ou inexistente."));

        if (!coupon.isValid()) {
            throw new RuntimeException("Este cupão já expirou ou atingiu o limite de uso.");
        }

        cart.setCoupon(coupon);
        repository.save(cart);
        
        log.info("Cupão {} aplicado ao carrinho de {}", code, user.getEmail());
        return mapToResponse(cart);
    }

    @Transactional
    public void clearCart() {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cart.setCoupon(null);
        repository.save(cart);
    }

    private Cart getOrCreateCart(User user) {
        return repository.findByUserEmail(user.getEmail())
                .orElseGet(() -> repository.save(Cart.builder().user(user).items(new ArrayList<>()).build()));
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizador não autenticado"));
    }

    /**
     * MAPEAMENTO: Refatorado para usar dados da Variant.
     */
    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .variantId(item.getVariant().getProduct().getId()) // ID do pai para referência
                        .productName(item.getVariant().getProduct().getName() + " - " + item.getVariant().getName()) // Nome completo
                        .productImageUrl(item.getVariant().getImageUrl()) // Foto da variante específica
                        .quantity(item.getQuantity())
                        .unitPrice(item.getVariant().getPrice()) // Preço da variante específica
                        .subtotal(item.getVariant().getPrice().multiply(new BigDecimal(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount = BigDecimal.ZERO;
        if (cart.getCoupon() != null && cart.getCoupon().isValid()) {
            discount = subtotal.multiply(BigDecimal.valueOf(cart.getCoupon().getDiscountPercentage() / 100));
        }

        BigDecimal shippingFee = new BigDecimal("25.00");
        String shippingMethod = "Correios (SEDEX/PAC)";
        
        if (subtotal.compareTo(freeShippingThreshold) >= 0) {
            shippingFee = BigDecimal.ZERO;
            shippingMethod += " - Grátis ✨";
        }

        return CartResponse.builder()
                .items(items)
                .subtotal(subtotal)
                .discountAmount(discount)
                .shippingFee(shippingFee)
                .shippingMethod(shippingMethod)
                .deliveryDays(7)
                .totalAmount(subtotal.subtract(discount).add(shippingFee))
                .appliedCoupon(cart.getCoupon() != null ? cart.getCoupon().getCode() : null)
                .build();
    }
}