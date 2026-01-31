package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.category.CategoryRequest;
import com.hygor.makeup_api.dto.category.CategoryResponse;
import com.hygor.makeup_api.exception.custom.BusinessException;
import com.hygor.makeup_api.exception.custom.ResourceNotFoundException;
import com.hygor.makeup_api.mapper.CategoryMapper; // Novo
import com.hygor.makeup_api.model.Category;
import com.hygor.makeup_api.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CategoryService extends BaseService<Category, CategoryRepository> {

    private final CategoryMapper categoryMapper; // Injeção do Mapper
    
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public CategoryService(CategoryRepository repository, CategoryMapper categoryMapper) {
        super(repository);
        this.categoryMapper = categoryMapper;
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (repository.existsByName(request.getName())) {
            throw new BusinessException("Já existe uma categoria com o nome: " + request.getName());
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .slug(generateSlug(request.getName()))
                .build();

        return categoryMapper.toResponse(repository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return repository.findAllByDeletedFalse().stream()
                .map(categoryMapper::toResponse) // Method Reference limpo
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getBySlug(String slug) {
        return repository.findBySlug(slug)
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com o slug: " + slug));
    }

    private String generateSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}