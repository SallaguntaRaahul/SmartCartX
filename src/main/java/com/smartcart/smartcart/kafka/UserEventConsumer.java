package com.smartcart.smartcart.kafka;

import com.smartcart.smartcart.config.KafkaConfig;
import com.smartcart.smartcart.dto.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserEventConsumer {

    @KafkaListener(topics = KafkaConfig.USER_REGISTERED_TOPIC,
            groupId = "welcome-email-group")
    public void handleUserRegistered(UserEvent event) {
        log.info(">>> WELCOME EMAIL: Sending welcome email to {}",
                event.getEmail());
        log.info(">>> WELCOME EMAIL: Dear {}, welcome to SmartCartX!",
                event.getFirstName());
        log.info(">>> WELCOME EMAIL: Email sent to {} successfully!",
                event.getEmail());
    }
}