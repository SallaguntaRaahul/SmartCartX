package com.smartcart.smartcart.kafka;

import com.smartcart.smartcart.dto.UserEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserEventConsumer Tests")
class UserEventConsumerTest {

    private UserEventConsumer userEventConsumer;

    @BeforeEach
    void setUp() {
        userEventConsumer = new UserEventConsumer();
    }

    @Test
    @DisplayName("Handle user registered - no exception")
    void handleUserRegistered_NoException() {
        UserEvent event = UserEvent.builder()
                .userId(1L)
                .email("raahul@test.com")
                .firstName("Raahul")
                .lastName("Sallagunta")
                .role("CUSTOMER")
                .eventType("USER_REGISTERED")
                .timestamp(LocalDateTime.now())
                .build();

        assertThatCode(() ->
                userEventConsumer
                        .handleUserRegistered(event))
                .doesNotThrowAnyException();
    }
}