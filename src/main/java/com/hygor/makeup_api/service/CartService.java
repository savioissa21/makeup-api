package com.hygor.makeup_api.service;

import com.hygor.makeup_api.model.Cart;
import com.hygor.makeup_api.model.CartItem;
import com.hygor.makeup_api.model.Product;
import com.hygor.makeup_api.repository.CartItemRepository;
import com.hygor.makeup_api.repository.CartRepository;
import com.hygor.makeup_api.repository.ProductRepository;
import com.hygor.makeup_api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CartService extends BaseService<Cart, CartRepository> {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository repository, 
                       CartItemRepository cartItemRepository, 
                       ProductRepository productRepository, 
                       UserRepository userRepository) {
        super(repository);
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Recupera o carrinho do utilizador logado ou cria um novo se não existir.
     */
    @Transactional
    public Cart getOrCreateCart(String email) {
        return repository.findByUserEmail(email)
                .orElseGet(() -> {
                    var user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("Utilizador não encontrado."));
                    Cart newCart = Cart.builder().user(user).build();
                    return repository.save(newCart);
                });
    }

    /**
     * Adiciona um produto ao carrinho com validação de stock.
     */
    @Transactional
    public Cart addItemToCart(String email, Long productId, Integer quantity) {
        Cart cart = getOrCreateCart(email);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));

        // Validação de Stock
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Stock insuficiente para o produto: " + product.getName());
        }

        // Verifica se o item já existe no carrinho para apenas somar a quantidade
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cart.addItem(newItem);
        }

        return repository.save(cart);
    }

    /**
     * Calcula o valor total de todos os itens no carrinho.
     */
    public BigDecimal calculateTotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> {
                    // Usa o preço de desconto se existir, caso contrário o preço normal
                    BigDecimal price = item.getProduct().getDiscountPrice() != null ? 
                                       item.getProduct().getDiscountPrice() : 
                                       item.getProduct().getPrice();
                    return price.multiply(new BigDecimal(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Remove um item específico do carrinho.
     */
    @Transactional
    public Cart removeItem(String email, Long cartItemId) {
        Cart cart = getOrCreateCart(email);
        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        return repository.save(cart);
    }
}