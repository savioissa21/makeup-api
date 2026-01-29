package com.hygor.makeup_api.repository;

import com.hygor.makeup_api.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Interface de acesso a dados para a entidade Product.
 * Inclui suporte a paginação e busca por SEO (slug).
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Busca um produto pelo slug para exibir na página de detalhes do site
    Optional<Product> findBySlug(String slug);

    // Lista produtos por categoria com suporte a paginação (Performance)
    Page<Product> findByCategorySlug(String categorySlug, Pageable pageable);

    // Busca produtos que contenham parte do nome (Barra de pesquisa)
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
}