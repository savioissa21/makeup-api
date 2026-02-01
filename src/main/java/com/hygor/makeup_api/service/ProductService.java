package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.product.ProductRequest;
import com.hygor.makeup_api.dto.product.ProductResponse;
import com.hygor.makeup_api.exception.custom.ResourceNotFoundException;
import com.hygor.makeup_api.mapper.ProductMapper;
import com.hygor.makeup_api.model.Brand;
import com.hygor.makeup_api.model.Category;
import com.hygor.makeup_api.model.Product;
import com.hygor.makeup_api.repository.BrandRepository;
import com.hygor.makeup_api.repository.CategoryRepository;
import com.hygor.makeup_api.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper;

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public ProductService(ProductRepository repository,
                          CategoryRepository categoryRepository,
                          BrandRepository brandRepository,
                          ProductMapper productMapper) {
        super(repository);
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productMapper = productMapper;
    }

    // --- LEITURAS (CACHEABLE) ---

    @Cacheable(value = "products", key = "'page_' + #pageable.pageNumber")
    public Page<ProductResponse> findAll(Pageable pageable) {
        log.info("Buscando produtos no banco de dados...");
        return repository.findAll(pageable).map(productMapper::toResponse);
    }

    @Cacheable(value = "product_details", key = "#id")
    public ProductResponse findById(Long id) {
        return repository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado ID: " + id));
    }

    @Transactional(readOnly = true)
    // Slug geralmente não cacheamos aqui ou criamos um cache separado "product_slug"
    public ProductResponse findBySlug(String slug) {
        return repository.findBySlug(slug)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + slug));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getFilteredProducts(String brandName, BigDecimal minPrice, BigDecimal maxPrice,
                                                     Double minRating, Pageable pageable) {
        // Filtros dinâmicos são difíceis de cachear efetivamente devido às infinitas combinações
        Double rating = (minRating == null) ? 0.0 : minRating;

        if (brandName != null && minPrice != null && maxPrice != null) {
            return repository
                    .findByBrandNameIgnoreCaseAndPriceBetweenAndRatingGreaterThanEqual(brandName, minPrice, maxPrice, rating, pageable)
                    .map(productMapper::toResponse);
        }
        return repository.findAll(pageable).map(productMapper::toResponse);
    }

    // --- ESCRITAS (CACHE EVICT) ---

    @Transactional
    @CacheEvict(value = { "products", "product_details" }, allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Criando produto: {}", request.getName());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada ID: " + request.getCategoryId()));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Marca não encontrada ID: " + request.getBrandId()));

        Product product = Product.builder()
                .name(request.getName())
                .brand(brand)
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .imagePrompt(request.getImagePrompt())
                .rating(0.0)
                .slug(generateSlug(request.getName()))
                .category(category)
                .build();

        return productMapper.toResponse(repository.save(product));
    }

    @Transactional
    @CacheEvict(value = { "products", "product_details" }, allEntries = true)
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("Atualizando produto ID: {}", id);

        Product product = findActiveById(id); // Garante que existe e não está deletado

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada ID: " + request.getCategoryId()));

        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Marca não encontrada ID: " + request.getBrandId()));

        // Atualiza os campos
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setImagePrompt(request.getImagePrompt());
        product.setCategory(category);
        product.setBrand(brand);
        
        // Se o nome mudar, regenera o slug (Opcional, mas bom para SEO)
        if (!product.getName().equals(request.getName())) {
             product.setSlug(generateSlug(request.getName()));
        }

        return productMapper.toResponse(repository.save(product));
    }
    
    @Transactional
    @CacheEvict(value = { "products", "product_details" }, allEntries = true)
    public ProductResponse updateProductImage(Long id, String imageUrl) {
        Product product = findActiveById(id);
        product.setImageUrl(imageUrl);
        return productMapper.toResponse(repository.save(product));
    }

    // Sobrescrevemos o delete para garantir que limpa o cache também!
    @Override
    @Transactional
    @CacheEvict(value = { "products", "product_details" }, allEntries = true)
    public void delete(Long id) {
        log.info("Deletando produto ID: {} e limpando cache", id);
        super.delete(id);
    }

    // --- UTILITÁRIOS ---

    private String generateSlug(String input) {
        if (input == null) return "";
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}