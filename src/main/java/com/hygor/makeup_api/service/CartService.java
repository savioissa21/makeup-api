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

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final CouponRepository couponRepository;

    // Puxa o valor do frete grátis do application.properties 🛠️ ✨
    @Value("${boutique.shipping.free-threshold:200.00}")
    private BigDecimal freeShippingThreshold;

    public CartService(CartRepository repository, 
                       ProductRepository productRepository, 
                       UserRepository userRepository, 
                       CartItemRepository cartItemRepository,
                       CouponRepository couponRepository) {
        super(repository);
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
        this.couponRepository = couponRepository;
    }

    @Transactional
    public CartResponse addItemToCart(CartItemRequest request) {
        User user = getAuthenticatedUser();
        Cart cart = getOrCreateCart(user);
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Quantidade solicitada indisponível em stock");
        }

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(CartItem.builder().cart(cart).product(product).quantity(0).build());

        cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        
        if (cartItem.getId() == null) {
            cart.getItems().add(cartItem);
        }

        repository.save(cart);
        log.info("Item {} adicionado ao carrinho de {}", product.getName(), user.getEmail());
        
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
     * MAPEAMENTO: Calcula subtotal, descontos e a Regra de Frete Grátis. 💎 ✨
     */
    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getProduct().getPrice())
                        .subtotal(item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 1. Cálculo do Desconto 🏷️
        BigDecimal discount = BigDecimal.ZERO;
        if (cart.getCoupon() != null && cart.getCoupon().isValid()) {
            discount = subtotal.multiply(BigDecimal.valueOf(cart.getCoupon().getDiscountPercentage() / 100));
        }

        // 2. Lógica de Frete Grátis 🚚 💨
        BigDecimal shippingFee = new BigDecimal("25.00"); // Frete padrão (Ex: PAC)
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
                .totalAmount(subtotal.subtract(discount).add(shippingFee)) // Soma o frete ao total 💰
                .appliedCoupon(cart.getCoupon() != null ? cart.getCoupon().getCode() : null)
                .build();
    }
}