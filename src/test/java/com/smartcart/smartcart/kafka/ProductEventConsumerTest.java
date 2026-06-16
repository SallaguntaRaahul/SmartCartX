package com.smartcart.smartcart.kafka;

import com.smartcart.smartcart.dto.ProductEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductEventConsumer Tests")
class ProductEventConsumerTest {

    private ProductEventConsumer productEventConsumer;

    @BeforeEach
    void setUp() {
        productEventConsumer = new ProductEventConsumer();
    }

    @Test
    @DisplayName("Handle low stock - no exception")
    void handleLowStock_NoException() {
        ProductEvent event = ProductEvent.builder()
                .productId(1L)
                .productName("iPhone 15 Pro")
                .stockQuantity(3)
                .eventType("LOW_STOCK")
                .timestamp(LocalDateTime.now())
                .build();

        assertThatCode(() ->
                productEventConsumer.handleLowStock(event))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Handle out of stock - no exception")
    void handleOutOfStock_NoException() {
        ProductEvent event = ProductEvent.builder()
                .productId(1L)
                .productName("iPhone 15 Pro")
                .stockQuantity(0)
                .eventType("OUT_OF_STOCK")
                .timestamp(LocalDateTime.now())
                .build();

        assertThatCode(() ->
                productEventConsumer
                        .handleOutOfStock(event))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Handle product created - no exception")
    void handleProductCreated_NoException() {
        ProductEvent event = ProductEvent.builder()
                .productId(1L)
                .productName("iPhone 15 Pro")
                .category("Electronics")
                .price(new BigDecimal("999.99"))
                .stockQuantity(50)
                .eventType("PRODUCT_CREATED")
                .timestamp(LocalDateTime.now())
                .build();

        assertThatCode(() ->
                productEventConsumer
                        .handleProductCreated(event))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Handle product updated - no exception")
    void handleProductUpdated_NoException() {
        ProductEvent event = ProductEvent.builder()
                .productId(1L)
                .productName("iPhone 15 Pro Updated")
                .category("Electronics")
                .price(new BigDecimal("1099.99"))
                .stockQuantity(45)
                .eventType("PRODUCT_UPDATED")
                .timestamp(LocalDateTime.now())
                .build();

        assertThatCode(() ->
                productEventConsumer
                        .handleProductUpdated(event))
                .doesNotThrowAnyException();
    }
}