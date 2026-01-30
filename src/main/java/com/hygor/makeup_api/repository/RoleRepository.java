package com.hygor.makeup_api.repository;

import com.hygor.makeup_api.model.Role;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends BaseEntityRepository<Role, Long> {
    Optional<Role> findByName(String name);
}