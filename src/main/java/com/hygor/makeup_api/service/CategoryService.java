package com.hygor.makeup_api.service;

import com.hygor.makeup_api.model.Category;
import com.hygor.makeup_api.repository.CategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoryService extends BaseService<Category, CategoryRepository> {
    public CategoryService(CategoryRepository repository) {
        super(repository);
    }
}