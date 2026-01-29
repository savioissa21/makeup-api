package com.hygor.makeup_api.service;

import com.hygor.makeup_api.model.Role;
import com.hygor.makeup_api.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleService extends BaseService<Role, RoleRepository> {
    public RoleService(RoleRepository repository) {
        super(repository);
    }
}