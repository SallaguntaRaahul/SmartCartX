package com.smartcart.smartcart.kafka;

import com.smartcart.smartcart.dto.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartEventConsumer Tests")
class CartEventConsumerTest {

    private CartEventConsumer cartEventConsumer;

    @BeforeEach
    void setUp() {
        cartEventConsumer = new CartEventConsumer();
    }

    @Test
    @DisplayName("Handle cart abandoned - no exception")
    void handleCartAbandoned_NoException() {
        CartEvent event = CartEvent.builder()
                .cartId(1L)
                .userId(1L)
                .customerEmail("raahul@test.com")
                .items(List.of())
                .totalPrice(new BigDecimal("999.99"))
                .eventType("CART_ABANDONED")
                .timestamp(LocalDateTime.now())
                .build();

        assertThatCode(() ->
                cartEventConsumer
                        .handleCartAbandoned(event))
                .doesNotThrowAnyException();
    }
}