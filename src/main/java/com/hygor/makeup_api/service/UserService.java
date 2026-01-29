package com.hygor.makeup_api.service;

import com.hygor.makeup_api.model.User;
import com.hygor.makeup_api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService extends BaseService<User, UserRepository> {

    public UserService(UserRepository repository) {
        super(repository);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilizador não encontrado com e-mail: " + email));
    }
}