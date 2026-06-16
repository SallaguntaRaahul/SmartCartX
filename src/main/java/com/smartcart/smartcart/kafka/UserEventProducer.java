package com.smartcart.smartcart.kafka;

import com.smartcart.smartcart.config.KafkaConfig;
import com.smartcart.smartcart.dto.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserRegistered(UserEvent event) {
        log.info(">>> Publishing USER_REGISTERED for: {}",
                event.getEmail());
        kafkaTemplate.send(KafkaConfig.USER_REGISTERED_TOPIC,
                event.getUserId().toString(), event);
    }
}