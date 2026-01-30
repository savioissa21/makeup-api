package com.hygor.makeup_api.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Representa um item específico dentro do carrinho de compras.
 */
@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false) // Alterado de product_id para variant_id
    private ProductVariant variant; // Agora aponta para a cor/tamanho específico

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
}