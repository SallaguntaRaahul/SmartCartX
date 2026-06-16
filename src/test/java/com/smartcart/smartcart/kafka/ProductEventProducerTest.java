package com.smartcart.smartcart.kafka;

import com.smartcart.smartcart.config.KafkaConfig;
import com.smartcart.smartcart.dto.ProductEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductEventProducer Tests")
class ProductEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private ProductEventProducer productEventProducer;

    private ProductEvent testProductEvent;

    @BeforeEach
    void setUp() {
        testProductEvent = ProductEvent.builder()
                .productId(1L)
                .productName("iPhone 15 Pro")
                .category("Electronics")
                .price(new BigDecimal("999.99"))
                .stockQuantity(50)
                .eventType("PRODUCT_CREATED")
                .timestamp(LocalDateTime.now())
                .build();

        CompletableFuture<SendResult<String, Object>>
                future = new CompletableFuture<>();
        when(kafkaTemplate.send(
                anyString(), anyString(), any()))
                .thenReturn(future);
    }

    @Test
    @DisplayName("Publish product created - correct topic")
    void publishProductCreated_CorrectTopic() {
        productEventProducer.publishProductCreated(
                testProductEvent);

        verify(kafkaTemplate).send(
                eq(KafkaConfig.PRODUCT_CREATED_TOPIC),
                eq("1"),
                eq(testProductEvent));
    }

    @Test
    @DisplayName("Publish product updated - correct topic")
    void publishProductUpdated_CorrectTopic() {
        productEventProducer.publishProductUpdated(
                testProductEvent);

        verify(kafkaTemplate).send(
                eq(KafkaConfig.PRODUCT_UPDATED_TOPIC),
                eq("1"),
                eq(testProductEvent));
    }

    @Test
    @DisplayName("Publish low stock alert - correct topic")
    void publishLowStockAlert_CorrectTopic() {
        ProductEvent lowStockEvent = ProductEvent.builder()
                .productId(1L)
                .productName("iPhone 15 Pro")
                .stockQuantity(3)
                .eventType("LOW_STOCK")
                .timestamp(LocalDateTime.now())
                .build();

        productEventProducer.publishLowStockAlert(
                lowStockEvent);

        verify(kafkaTemplate).send(
                eq(KafkaConfig.LOW_STOCK_ALERT_TOPIC),
                eq("1"),
                eq(lowStockEvent));
    }

    @Test
    @DisplayName("Publish out of stock - correct topic")
    void publishOutOfStock_CorrectTopic() {
        ProductEvent outOfStockEvent = ProductEvent.builder()
                .productId(1L)
                .productName("iPhone 15 Pro")
                .stockQuantity(0)
                .eventType("OUT_OF_STOCK")
                .timestamp(LocalDateTime.now())
                .build();

        productEventProducer.publishOutOfStock(
                outOfStockEvent);

        verify(kafkaTemplate).send(
                eq(KafkaConfig.OUT_OF_STOCK_TOPIC),
                eq("1"),
                eq(outOfStockEvent));
    }
}