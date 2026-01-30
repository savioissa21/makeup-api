package com.hygor.makeup_api.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade para persistência do carrinho de compras no servidor.
 * Garante que os dados não se percam ao recarregar a página.
 */
@Entity
@Table(name = "carts")
@SQLRestriction("deleted = false")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    /**
     * Helper para adicionar item e manter a consistência da relação bidirecional.
     */
    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }
}