package com.smartcart.smartcart.kafka;

import com.smartcart.smartcart.config.KafkaConfig;
import com.smartcart.smartcart.dto.CartEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCartAbandoned(CartEvent event) {
        log.info(">>> Publishing CART_ABANDONED for user: {}",
                event.getCustomerEmail());
        kafkaTemplate.send(KafkaConfig.CART_ABANDONED_TOPIC,
                event.getCartId().toString(), event);
    }
}