package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.product.ProductRequest;
import com.hygor.makeup_api.dto.product.ProductResponse;
import com.hygor.makeup_api.dto.product.ProductStockRequest;
import com.hygor.makeup_api.model.Category;
import com.hygor.makeup_api.model.Product;
import com.hygor.makeup_api.repository.CategoryRepository;
import com.hygor.makeup_api.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ProductService extends BaseService<Product, ProductRepository> {

    private final CategoryRepository categoryRepository;
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public ProductService(ProductRepository repository, CategoryRepository categoryRepository) {
        super(repository);
        this.categoryRepository = categoryRepository;
    }

    /**
     * Cria um novo produto associando à categoria e gerando slug automático.
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .brand(request.getBrand())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .stockQuantity(request.getStockQuantity())
                .imagePrompt(request.getImagePrompt())
                .rating(0.0) 
                .slug(generateSlug(request.getName()))
                .category(category)
                .build();

        Product saved = repository.save(product);
        log.info("Produto criado com sucesso: {} (Slug: {})", saved.getName(), saved.getSlug());
        return toResponse(saved); // Renomeado
    }

    /**
     * Busca filtrada com paginação.
     */
    @Transactional(readOnly = true)
    public Page<Product> getFilteredProducts(String brand, BigDecimal minPrice, BigDecimal maxPrice, Double minRating, Pageable pageable) {
        Double rating = (minRating == null) ? 0.0 : minRating;
        
        if (brand != null && minPrice != null && maxPrice != null) {
            return repository.findByBrandIgnoreCaseAndPriceBetweenAndRatingGreaterThanEqual(brand, minPrice, maxPrice, rating, pageable);
        }
        
        return repository.findAll(pageable);
    }

    /**
     * Atualiza apenas o estoque.
     */
    @Transactional
    public ProductResponse updateStock(Long id, ProductStockRequest request) {
        Product product = findActiveById(id);
        product.setStockQuantity(request.getStockQuantity());
        return toResponse(repository.save(product)); // Renomeado
    }

    /**
     * RENOMEADO: Agora coincide com a chamada do Controller.
     */
    @Transactional(readOnly = true)
    public ProductResponse findBySlug(String slug) {
        Product product = repository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com o slug: " + slug));
        return toResponse(product);
    }

    /**
     * Gerador de Slug.
     */
    private String generateSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    /**
     * RENOMEADO e PUBLIC: Para ser usado no .map() do Controller.
     */
    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .rating(product.getRating())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : "Sem Categoria")
                .build();
    }
}