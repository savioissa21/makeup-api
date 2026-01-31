package com.hygor.makeup_api.service;

import com.hygor.makeup_api.exception.custom.ResourceNotFoundException;
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
     * Segurança: Adicionar verificação de dono do carrinho seria ideal aqui.
     */
    @Transactional
    public void removeItem(Long itemId) {
        if (!repository.existsById(itemId)) {
            throw new ResourceNotFoundException("Item não encontrado no carrinho: " + itemId);
        }
        repository.deleteById(itemId);
        log.info("Item ID {} removido do carrinho.", itemId);
    }

    /**
     * Atualiza a quantidade de um item.
     * Se a quantidade for <= 0, remove o item.
     */
    @Transactional
    public void updateQuantity(Long itemId, Integer quantity) {
        CartItem item = repository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item não encontrado: " + itemId));

        if (quantity <= 0) {
            repository.delete(item);
            log.info("Item {} removido (quantidade zerada).", itemId);
        } else {
            item.setQuantity(quantity);
            repository.save(item);
            log.info("Quantidade do item {} atualizada para {}.", itemId, quantity);
        }
    }

    @Transactional(readOnly = true)
    public List<CartItem> findByCartId(Long cartId) {
        return repository.findByCartId(cartId);
    }
}