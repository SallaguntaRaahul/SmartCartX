package com.smartcart.smartcart.kafka;

import com.smartcart.smartcart.config.KafkaConfig;
import com.smartcart.smartcart.dto.UserEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserEventProducer Tests")
class UserEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private UserEventProducer userEventProducer;

    private UserEvent testUserEvent;

    @BeforeEach
    void setUp() {
        testUserEvent = UserEvent.builder()
                .userId(1L)
                .email("raahul@test.com")
                .firstName("Raahul")
                .lastName("Sallagunta")
                .role("CUSTOMER")
                .eventType("USER_REGISTERED")
                .timestamp(LocalDateTime.now())
                .build();

        CompletableFuture<SendResult<String, Object>>
                future = new CompletableFuture<>();
        when(kafkaTemplate.send(
                anyString(), anyString(), any()))
                .thenReturn(future);
    }

    @Test
    @DisplayName("Publish user registered - correct topic")
    void publishUserRegistered_CorrectTopic() {
        userEventProducer.publishUserRegistered(
                testUserEvent);

        verify(kafkaTemplate).send(
                eq(KafkaConfig.USER_REGISTERED_TOPIC),
                eq("1"),
                eq(testUserEvent));
    }

    @Test
    @DisplayName("Publish user registered - called once")
    void publishUserRegistered_CalledOnce() {
        userEventProducer.publishUserRegistered(
                testUserEvent);

        verify(kafkaTemplate, times(1))
                .send(anyString(), anyString(), any());
    }
}