package com.smartcart.smartcart.kafka;

import com.smartcart.smartcart.dto.OrderEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationConsumer Tests")
class NotificationConsumerTest {

    private NotificationConsumer notificationConsumer;

    @BeforeEach
    void setUp() {
        notificationConsumer = new NotificationConsumer();
    }

    @Test
    @DisplayName("Handle order placed - no exception")
    void handleOrderPlaced_NoException() {
        OrderEvent event = OrderEvent.builder()
                .orderId(1L)
                .customerEmail("raahul@test.com")
                .status("PLACED")
                .totalAmount(new BigDecimal("999.99"))
                .items(List.of())
                .timestamp(LocalDateTime.now())
                .build();

        assertThatCode(() ->
                notificationConsumer.handleOrderPlaced(event))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Handle order cancelled - no exception")
    void handleOrderCancelled_NoException() {
        OrderEvent event = OrderEvent.builder()
                .orderId(1L)
                .customerEmail("raahul@test.com")
                .status("CANCELLED")
                .totalAmount(new BigDecimal("999.99"))
                .items(List.of())
                .timestamp(LocalDateTime.now())
                .build();

        assertThatCode(() ->
                notificationConsumer
                        .handleOrderCancelled(event))
                .doesNotThrowAnyException();
    }
}