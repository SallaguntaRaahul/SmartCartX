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
@DisplayName("OrderEventProducer Tests")
class OrderEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderEventProducer orderEventProducer;

    private OrderEvent testOrderEvent;

    @BeforeEach
    void setUp() {
        testOrderEvent = OrderEvent.builder()
                .orderId(1L)
                .customerEmail("raahul@test.com")
                .status("PLACED")
                .totalAmount(new BigDecimal("999.99"))
                .items(List.of())
                .timestamp(LocalDateTime.now())
                .build();

        // Mock KafkaTemplate.send() to return
        // a CompletableFuture so .whenComplete()
        // doesn't throw NullPointerException
        CompletableFuture<SendResult<String, Object>>
                future = new CompletableFuture<>();
        when(kafkaTemplate.send(
                anyString(), anyString(), any()))
                .thenReturn(future);
    }

    @Test
    @DisplayName("Publish order placed - sends to correct topic")
    void publishOrderPlaced_SendsToCorrectTopic() {
        orderEventProducer.publishOrderPlaced(testOrderEvent);

        verify(kafkaTemplate).send(
                eq(KafkaConfig.ORDER_PLACED_TOPIC),
                eq("1"),
                eq(testOrderEvent));
    }

    @Test
    @DisplayName("Publish order cancelled - sends to correct topic")
    void publishOrderCancelled_SendsToCorrectTopic() {
        OrderEvent cancelEvent = OrderEvent.builder()
                .orderId(1L)
                .customerEmail("raahul@test.com")
                .status("CANCELLED")
                .totalAmount(new BigDecimal("999.99"))
                .items(List.of())
                .timestamp(LocalDateTime.now())
                .build();

        orderEventProducer.publishOrderCancelled(cancelEvent);

        verify(kafkaTemplate).send(
                eq(KafkaConfig.ORDER_CANCELLED_TOPIC),
                eq("1"),
                eq(cancelEvent));
    }

    @Test
    @DisplayName("Publish order placed - called once")
    void publishOrderPlaced_CalledOnce() {
        orderEventProducer.publishOrderPlaced(testOrderEvent);

        verify(kafkaTemplate, times(1))
                .send(anyString(), anyString(), any());
    }
}