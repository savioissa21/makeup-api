package com.hygor.makeup_api.repository;

import com.hygor.makeup_api.model.Category;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Interface de acesso a dados para a entidade Category.
 */
@Repository
public interface CategoryRepository extends BaseEntityRepository<Category, Long> {

    // Busca categoria pelo slug para filtrar produtos na vitrine
    Optional<Category> findBySlug(String slug);

    // Verifica se já existe uma categoria com determinado nome
    boolean existsByName(String name);
}