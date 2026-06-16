package com.smartcart.smartcart.kafka;

import com.smartcart.smartcart.config.KafkaConfig;
import com.smartcart.smartcart.dto.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationConsumer {

    @KafkaListener(
            topics = KafkaConfig.ORDER_PLACED_TOPIC,
            groupId = "notification-group")
    public void handleOrderPlaced(OrderEvent event) {
        log.info(">>> NOTIFICATION SERVICE: Sending confirmation " +
                "email to {}", event.getCustomerEmail());
        log.info(">>> NOTIFICATION SERVICE: Order #{} confirmed " +
                "for ${}", event.getOrderId(), event.getTotalAmount());
        log.info(">>> NOTIFICATION SERVICE: Email sent successfully!");
    }

    @KafkaListener(
            topics = KafkaConfig.ORDER_CANCELLED_TOPIC,
            groupId = "notification-group")
    public void handleOrderCancelled(OrderEvent event) {
        log.info(">>> NOTIFICATION SERVICE: Sending cancellation " +
                "email to {}", event.getCustomerEmail());
        log.info(">>> NOTIFICATION SERVICE: Order #{} cancelled",
                event.getOrderId());
    }
}