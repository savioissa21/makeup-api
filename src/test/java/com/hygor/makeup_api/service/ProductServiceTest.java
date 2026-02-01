package com.hygor.makeup_api.service;

import com.hygor.makeup_api.dto.product.ProductResponse;
import com.hygor.makeup_api.exception.custom.ResourceNotFoundException;
import com.hygor.makeup_api.gateway.FileStorageGateway;
import com.hygor.makeup_api.mapper.ProductMapper;
import com.hygor.makeup_api.model.Product;
import com.hygor.makeup_api.repository.BrandRepository;
import com.hygor.makeup_api.repository.CategoryRepository;
import com.hygor.makeup_api.repository.ProductRepository;
import com.hygor.makeup_api.repository.ProductVariantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductVariantRepository variantRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private BrandRepository brandRepository;
    @Mock private FileStorageGateway fileStorageGateway;
    @Mock private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("Deve buscar produto pelo Slug com sucesso")
    void shouldFindProductBySlug() {
        // --- GIVEN ---
        String slug = "batom-vermelho-matte";
        Product product = new Product();
        product.setName("Batom Vermelho");
        product.setSlug(slug);

        when(productRepository.findBySlug(slug)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(new ProductResponse());

        // --- WHEN ---
        var result = productService.findBySlug(slug);

        // --- THEN ---
        assertNotNull(result);
        verify(productRepository).findBySlug(slug);
    }

    @Test
    @DisplayName("Deve lançar erro ao buscar Slug inexistente")
    void shouldThrowException_WhenSlugNotFound() {
        when(productRepository.findBySlug("inexistente")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> productService.findBySlug("inexistente"));
    }

    @Test
    @DisplayName("Upload de Imagem: Deve salvar nova e DELETAR antiga")
    void shouldUpdateProductImage_AndCleanupOldOne() {
        // --- GIVEN ---
        Long productId = 1L;
        String oldImageUrl = "https://cloudinary.com/antiga.jpg";
        String newImageUrl = "https://cloudinary.com/nova.jpg";

        Product product = new Product();
        product.setId(productId);
        product.setImageUrl(oldImageUrl); 
        // [CORREÇÃO]: Não existe setActive. O produto já nasce ativo (deleted=false) por padrão no BaseEntity.

        MultipartFile file = mock(MultipartFile.class);

        // [CORREÇÃO]: O BaseService chama findByIdAndDeletedFalse, então temos que mockar ESSE método.
        when(productRepository.findByIdAndDeletedFalse(productId)).thenReturn(Optional.of(product));
        
        when(fileStorageGateway.uploadFile(file, "products")).thenReturn(newImageUrl);
        when(productRepository.save(product)).thenAnswer(i -> i.getArgument(0));
        when(productMapper.toResponse(any())).thenReturn(new ProductResponse());

        // --- WHEN ---
        productService.updateProductImage(productId, file);

        // --- THEN ---
        assertEquals(newImageUrl, product.getImageUrl()); // Verifica se atualizou a URL
        verify(fileStorageGateway, times(1)).deleteFile(oldImageUrl); // Verifica se limpou a antiga
        verify(productRepository).save(product); // Verifica se salvou
    }
}