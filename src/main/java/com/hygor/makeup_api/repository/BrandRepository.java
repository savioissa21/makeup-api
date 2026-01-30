package com.hygor.makeup_api.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.hygor.makeup_api.model.Brand;

@Repository
public interface BrandRepository extends BaseEntityRepository<Brand, Long> {
    Optional<Brand> findBySlug(String slug);

    boolean existsByName(String name);
}