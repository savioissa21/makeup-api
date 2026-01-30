package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.category.CategoryRequest;
import com.hygor.makeup_api.dto.category.CategoryResponse;
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

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public CategoryService(CategoryRepository repository) {
        super(repository);
    }

    /**
     * Cria uma nova categoria com geração automática de slug.
     */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        // 1. Validação de duplicidade por nome
        if (repository.existsByName(request.getName())) {
            throw new RuntimeException("Já existe uma categoria com o nome: " + request.getName());
        }

        // 2. Construção da entidade com Slug automático para SEO
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .slug(generateSlug(request.getName()))
                .build();

        Category saved = repository.save(category);
        log.info("Categoria criada com sucesso: {} (Slug: {})", saved.getName(), saved.getSlug());
        
        return mapToResponse(saved);
    }

    /**
     * Lista todas as categorias ativas convertidas para DTO.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return repository.findAllByDeletedFalse().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca uma categoria pelo slug para filtros na vitrine.
     */
    @Transactional(readOnly = true)
    public CategoryResponse getBySlug(String slug) {
        Category category = repository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com o slug: " + slug));
        return mapToResponse(category);
    }

    /**
     * Gerador de Slug (Igual ao do ProductService para manter consistência).
     */
    private String generateSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .build();
    }
}