package com.hygor.makeup_api.service;

import com.hygor.makeup_api.model.CartItem;
import com.hygor.makeup_api.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço responsável por operações granulares nos itens do carrinho.
 */
@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository repository;

    /**
     * Atualiza a quantidade de um item específico que já está no carrinho.
     */
    @Transactional
    public CartItem updateQuantity(Long itemId, Integer newQuantity) {
        if (newQuantity <= 0) {
            throw new RuntimeException("A quantidade deve ser maior que zero.");
        }

        CartItem item = repository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item do carrinho não encontrado."));

        item.setQuantity(newQuantity);
        return repository.save(item);
    }

    /**
     * Procura um item específico pelo ID.
     */
    @Transactional(readOnly = true)
    public CartItem findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item não encontrado."));
    }

    /**
     * Remove fisicamente o item da base de dados.
     * Nota: Itens de carrinho geralmente não usam Soft Delete pois são temporários.
     */
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Não foi possível remover: Item inexistente.");
        }
        repository.deleteById(id);
    }
}