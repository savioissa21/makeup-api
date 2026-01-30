package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.product.ProductRequest;
import com.hygor.makeup_api.dto.product.ProductResponse;
import com.hygor.makeup_api.dto.product.ProductStockRequest;
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
    private final BrandRepository brandRepository; // Novo: Repositório de Marcas
    
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public ProductService(ProductRepository repository, 
                          CategoryRepository categoryRepository, 
                          BrandRepository brandRepository) {
        super(repository);
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    /**
     * Cria um novo produto com validação de Categoria e Marca.
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Iniciando criação do produto: {}", request.getName());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + request.getCategoryId()));

        // Validação da nova entidade Brand
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new RuntimeException("Marca não encontrada com ID: " + request.getBrandId()));

        Product product = Product.builder()
                .name(request.getName())
                .brand(brand) // Agora associa o objeto Brand
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
        return toResponse(saved);
    }

    /**
     * Busca filtrada utilizando os novos relacionamentos.
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getFilteredProducts(String brandName, BigDecimal minPrice, BigDecimal maxPrice, Double minRating, Pageable pageable) {
        Double rating = (minRating == null) ? 0.0 : minRating;
        
        // Se houver filtro de marca, usamos a busca por nome ignore case
        if (brandName != null && minPrice != null && maxPrice != null) {
            return repository.findByBrandNameIgnoreCaseAndPriceBetweenAndRatingGreaterThanEqual(brandName, minPrice, maxPrice, rating, pageable)
                    .map(this::toResponse);
        }
        
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public ProductResponse updateStock(Long id, ProductStockRequest request) {
        Product product = findActiveById(id);
        product.setStockQuantity(request.getStockQuantity());
        log.info("Stock atualizado para o produto ID {}: novo total {}", id, request.getStockQuantity());
        return toResponse(repository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse findBySlug(String slug) {
        return repository.findBySlug(slug)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com o slug: " + slug));
    }

    @Transactional
    public ProductResponse updateProductImage(Long id, String imageUrl) {
        Product product = findActiveById(id);
        product.setImageUrl(imageUrl);
        return toResponse(repository.save(product));
    }

    /**
     * Converte a Entidade para DTO - Centralizado para facilitar manutenção.
     */
    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .brandName(product.getBrand() != null ? product.getBrand().getName() : "Marca Desconhecida")
                .brandLogo(product.getBrand() != null ? product.getBrand().getLogoUrl() : null)
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .discountPrice(product.getDiscountPrice())
                .rating(product.getRating())
                .imageUrl(product.getImageUrl())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : "Sem Categoria")
                .build();
    }

    private String generateSlug(String input) {
        if (input == null) return "";
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }
}