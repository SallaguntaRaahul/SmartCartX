package com.smartcart.smartcart.kafka;

import com.smartcart.smartcart.config.KafkaConfig;
import com.smartcart.smartcart.dto.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartEventProducer Tests")
class CartEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private CartEventProducer cartEventProducer;

    private CartEvent testCartEvent;

    @BeforeEach
    void setUp() {
        testCartEvent = CartEvent.builder()
                .cartId(1L)
                .userId(1L)
                .customerEmail("raahul@test.com")
                .items(List.of())
                .totalPrice(new BigDecimal("999.99"))
                .eventType("CART_ABANDONED")
                .timestamp(LocalDateTime.now())
                .build();

        CompletableFuture<SendResult<String, Object>>
                future = new CompletableFuture<>();
        when(kafkaTemplate.send(
                anyString(), anyString(), any()))
                .thenReturn(future);
    }

    @Test
    @DisplayName("Publish cart abandoned - correct topic")
    void publishCartAbandoned_CorrectTopic() {
        cartEventProducer.publishCartAbandoned(
                testCartEvent);

        verify(kafkaTemplate).send(
                eq(KafkaConfig.CART_ABANDONED_TOPIC),
                eq("1"),
                eq(testCartEvent));
    }

    @Test
    @DisplayName("Publish cart abandoned - called once")
    void publishCartAbandoned_CalledOnce() {
        cartEventProducer.publishCartAbandoned(
                testCartEvent);

        verify(kafkaTemplate, times(1))
                .send(anyString(), anyString(), any());
    }
}