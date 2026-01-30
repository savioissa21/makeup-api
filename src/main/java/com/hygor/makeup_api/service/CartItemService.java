package com.hygor.makeup_api.service;

import com.hygor.makeup_api.model.CartItem;
import com.hygor.makeup_api.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartItemService {

    private final CartItemRepository repository;

    /**
     * Remove um item específico do carrinho.
     */
    @Transactional
    public void removeItem(Long itemId) {
        if (!repository.existsById(itemId)) {
            throw new RuntimeException("Item não encontrado no carrinho.");
        }
        repository.deleteById(itemId);
        log.info("Item ID {} removido do carrinho com sucesso.", itemId);
    }

    /**
     * Atualiza a quantidade de um item já existente.
     */
    @Transactional
    public void updateQuantity(Long itemId, Integer quantity) {
        CartItem item = repository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item não encontrado."));

        if (quantity <= 0) {
            repository.delete(item);
            log.info("Item removido pois a quantidade definida foi zero ou negativa.");
        } else {
            item.setQuantity(quantity);
            repository.save(item);
            log.info("Quantidade do item ID {} atualizada para {}.", itemId, quantity);
        }
    }

    /**
     * Procura itens de um carrinho específico.
     */
    @Transactional(readOnly = true)
    public List<CartItem> findByCartId(Long cartId) {
        return repository.findByCartId(cartId);
    }
}