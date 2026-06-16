package com.smartcart.smartcart.service;

import com.smartcart.smartcart.dto.ProductDTO;
import com.smartcart.smartcart.exception.ResourceNotFoundException;
import com.smartcart.smartcart.kafka.ProductEventProducer;
import com.smartcart.smartcart.model.Product;
import com.smartcart.smartcart.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.*;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductEventProducer productEventProducer;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private ProductDTO testProductDTO;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .name("iPhone 15 Pro")
                .description("Apple smartphone")
                .price(new BigDecimal("999.99"))
                .stockQuantity(50)
                .category("Electronics")
                .build();

        testProductDTO = ProductDTO.builder()
                .id(1L)
                .name("iPhone 15 Pro")
                .description("Apple smartphone")
                .price(new BigDecimal("999.99"))
                .stockQuantity(50)
                .category("Electronics")
                .build();
    }

    @Test
    @DisplayName("Get product by ID - Success")
    void getProductById_Success() {
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(testProduct));

        ProductDTO result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("iPhone 15 Pro");
        assertThat(result.getPrice())
                .isEqualByComparingTo(new BigDecimal("999.99"));
        assertThat(result.getCategory()).isEqualTo("Electronics");

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Get product by ID - Not Found")
    void getProductById_NotFound() {
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Create product - Success")
    void createProduct_Success() {
        when(productRepository.save(any(Product.class)))
                .thenReturn(testProduct);
        doNothing().when(productEventProducer)
                .publishProductCreated(any());

        ProductDTO result = productService
                .createProduct(testProductDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("iPhone 15 Pro");
        assertThat(result.getPrice())
                .isEqualByComparingTo(new BigDecimal("999.99"));

        verify(productRepository, times(1))
                .save(any(Product.class));
        verify(productEventProducer, times(1))
                .publishProductCreated(any());
    }

    @Test
    @DisplayName("Update product - Success")
    void updateProduct_Success() {
        Product updatedProduct = Product.builder()
                .id(1L)
                .name("iPhone 15 Pro Max")
                .description("Updated")
                .price(new BigDecimal("1199.99"))
                .stockQuantity(30)
                .category("Electronics")
                .build();

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class)))
                .thenReturn(updatedProduct);
        doNothing().when(productEventProducer)
                .publishProductUpdated(any());

        ProductDTO updateDTO = ProductDTO.builder()
                .name("iPhone 15 Pro Max")
                .description("Updated")
                .price(new BigDecimal("1199.99"))
                .stockQuantity(30)
                .category("Electronics")
                .build();

        ProductDTO result = productService
                .updateProduct(1L, updateDTO);

        assertThat(result.getName())
                .isEqualTo("iPhone 15 Pro Max");
        assertThat(result.getPrice())
                .isEqualByComparingTo(new BigDecimal("1199.99"));

        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
        verify(productEventProducer).publishProductUpdated(any());
    }

    @Test
    @DisplayName("Update product - Not Found")
    void updateProduct_NotFound() {
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                productService.updateProduct(999L, testProductDTO))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    @DisplayName("Delete product - Success")
    void deleteProduct_Success() {
        when(productRepository.existsById(1L))
                .thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        assertThatCode(() ->
                productService.deleteProduct(1L))
                .doesNotThrowAnyException();

        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Delete product - Not Found")
    void deleteProduct_NotFound() {
        when(productRepository.existsById(999L))
                .thenReturn(false);

        assertThatThrownBy(() ->
                productService.deleteProduct(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Get all products - Paginated")
    void getAllProducts_Paginated() {
        Page<Product> page = new PageImpl<>(
                List.of(testProduct));
        when(productRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<ProductDTO> result =
                productService.getAllProducts(0, 10, "id");

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName())
                .isEqualTo("iPhone 15 Pro");

        verify(productRepository)
                .findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Search products by name")
    void searchProducts_ByName() {
        Page<Product> page = new PageImpl<>(
                List.of(testProduct));
        when(productRepository
                .findByNameContainingIgnoreCase(
                        eq("iPhone"), any(Pageable.class)))
                .thenReturn(page);

        Page<ProductDTO> result =
                productService.searchProducts("iPhone", 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName())
                .contains("iPhone");

        verify(productRepository)
                .findByNameContainingIgnoreCase(
                        eq("iPhone"), any(Pageable.class));
    }

    @Test
    @DisplayName("Get products by category")
    void getProductsByCategory() {
        Page<Product> page = new PageImpl<>(
                List.of(testProduct));
        when(productRepository.findByCategory(
                eq("Electronics"), any(Pageable.class)))
                .thenReturn(page);

        Page<ProductDTO> result =
                productService.getProductsByCategory(
                        "Electronics", 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategory())
                .isEqualTo("Electronics");
    }
}