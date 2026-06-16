package com.smartcart.smartcart.kafka;

import com.smartcart.smartcart.config.KafkaConfig;
import com.smartcart.smartcart.dto.ProductEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishProductCreated(ProductEvent event) {
        log.info(">>> Publishing PRODUCT_CREATED for: {}",
                event.getProductName());
        kafkaTemplate.send(KafkaConfig.PRODUCT_CREATED_TOPIC,
                event.getProductId().toString(), event);
    }

    public void publishProductUpdated(ProductEvent event) {
        log.info(">>> Publishing PRODUCT_UPDATED for: {}",
                event.getProductName());
        kafkaTemplate.send(KafkaConfig.PRODUCT_UPDATED_TOPIC,
                event.getProductId().toString(), event);
    }

    public void publishLowStockAlert(ProductEvent event) {
        log.warn(">>> Publishing LOW_STOCK_ALERT for: {} " +
                        "(remaining: {})",
                event.getProductName(), event.getStockQuantity());
        kafkaTemplate.send(KafkaConfig.LOW_STOCK_ALERT_TOPIC,
                event.getProductId().toString(), event);
    }

    public void publishOutOfStock(ProductEvent event) {
        log.warn(">>> Publishing OUT_OF_STOCK for: {}",
                event.getProductName());
        kafkaTemplate.send(KafkaConfig.OUT_OF_STOCK_TOPIC,
                event.getProductId().toString(), event);
    }
}