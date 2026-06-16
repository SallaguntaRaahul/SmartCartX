package com.smartcart.smartcart.repository;

import com.smartcart.smartcart.model.Product;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.data.domain.*;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@DisplayName("ProductRepository Tests")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = productRepository.save(
                Product.builder()
                        .name("iPhone 15 Pro")
                        .description("Apple phone")
                        .price(new BigDecimal("999.99"))
                        .stockQuantity(50)
                        .category("Electronics")
                        .build());
    }

    @Test
    @DisplayName("Find by name containing - found")
    void findByNameContaining_Found() {
        Page<Product> result = productRepository
                .findByNameContainingIgnoreCase(
                        "iPhone",
                        PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName())
                .isEqualTo("iPhone 15 Pro");
    }

    @Test
    @DisplayName("Find by name containing - not found")
    void findByNameContaining_NotFound() {
        Page<Product> result = productRepository
                .findByNameContainingIgnoreCase(
                        "Samsung",
                        PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Find by category - found")
    void findByCategory_Found() {
        Page<Product> result = productRepository
                .findByCategory(
                        "Electronics",
                        PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)
                .getCategory())
                .isEqualTo("Electronics");
    }

    @Test
    @DisplayName("Find by category - not found")
    void findByCategory_NotFound() {
        Page<Product> result = productRepository
                .findByCategory(
                        "Laptops",
                        PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Save product - success")
    void saveProduct_Success() {
        Product saved = productRepository.save(
                Product.builder()
                        .name("MacBook Pro")
                        .description("Apple laptop")
                        .price(new BigDecimal("1999.99"))
                        .stockQuantity(20)
                        .category("Laptops")
                        .build());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName())
                .isEqualTo("MacBook Pro");
    }

    @Test
    @DisplayName("Delete product - success")
    void deleteProduct_Success() {
        productRepository.deleteById(testProduct.getId());

        assertThat(productRepository
                .findById(testProduct.getId()))
                .isEmpty();
    }

    @Test
    @DisplayName("Exists by id - true")
    void existsById_True() {
        assertThat(productRepository
                .existsById(testProduct.getId()))
                .isTrue();
    }

    @Test
    @DisplayName("Exists by id - false")
    void existsById_False() {
        assertThat(productRepository
                .existsById(999L))
                .isFalse();
    }
}