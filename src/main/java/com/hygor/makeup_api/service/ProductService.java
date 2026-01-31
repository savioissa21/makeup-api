package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.product.ProductRequest;
import com.hygor.makeup_api.dto.product.ProductResponse;
import com.hygor.makeup_api.exception.custom.ResourceNotFoundException;
import com.hygor.makeup_api.mapper.ProductMapper; // Novo
import com.hygor.makeup_api.model.Brand;
import com.hygor.makeup_api.model.Category;
import com.hygor.makeup_api.model.Product;
import com.hygor.makeup_api.repository.BrandRepository;
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
    private final BrandRepository brandRepository;
    private final ProductMapper productMapper; // A estrela do show ⭐

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    // Construtor muito mais leve!
    public ProductService(ProductRepository repository,
                         CategoryRepository categoryRepository,
                         BrandRepository brandRepository,
                         ProductMapper productMapper) {
        super(repository);
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Iniciando criação do produto: {}", request.getName());

        // Usamos ResourceNotFoundException para dar 404 se falhar
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

        Product saved = repository.save(product);
        
        // Uma linha mágica substitui 20 linhas de conversão manual
        return productMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getFilteredProducts(String brandName, BigDecimal minPrice, BigDecimal maxPrice,
                                                     Double minRating, Pageable pageable) {
        Double rating = (minRating == null) ? 0.0 : minRating;

        if (brandName != null && minPrice != null && maxPrice != null) {
            return repository
                    .findByBrandNameIgnoreCaseAndPriceBetweenAndRatingGreaterThanEqual(brandName, minPrice, maxPrice, rating, pageable)
                    .map(productMapper::toResponse);
        }

        return repository.findAll(pageable).map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse findBySlug(String slug) {
        return repository.findBySlug(slug)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + slug));
    }

    @Transactional
    public ProductResponse updateProductImage(Long id, String imageUrl) {
        Product product = findActiveById(id);
        product.setImageUrl(imageUrl);
        return productMapper.toResponse(repository.save(product));
    }

    private String generateSlug(String input) {
        if (input == null) return "";
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}