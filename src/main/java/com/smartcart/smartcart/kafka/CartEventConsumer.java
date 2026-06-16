package com.smartcart.smartcart.kafka;

import com.smartcart.smartcart.config.KafkaConfig;
import com.smartcart.smartcart.dto.CartEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CartEventConsumer {

    @KafkaListener(topics = KafkaConfig.CART_ABANDONED_TOPIC,
            groupId = "cart-reminder-group")
    public void handleCartAbandoned(CartEvent event) {
        log.info(">>> CART REMINDER: User {} has abandoned cart " +
                        "with {} items worth ${}",
                event.getCustomerEmail(),
                event.getItems().size(),
                event.getTotalPrice());
        log.info(">>> CART REMINDER: Sending reminder email to {}",
                event.getCustomerEmail());
        log.info(">>> CART REMINDER: Email sent - " +
                "Come back and complete your purchase!");
    }
}