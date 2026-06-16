package com.smartcart.smartcart.kafka;

import com.smartcart.smartcart.config.KafkaConfig;
import com.smartcart.smartcart.dto.ProductEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductEventConsumer {

    @KafkaListener(topics = KafkaConfig.LOW_STOCK_ALERT_TOPIC,
            groupId = "admin-alert-group")
    public void handleLowStock(ProductEvent event) {
        log.warn(">>> ADMIN ALERT: Low stock for {} - " +
                        "only {} units remaining!",
                event.getProductName(), event.getStockQuantity());
        log.warn(">>> ADMIN ALERT: Please restock {} immediately!",
                event.getProductName());
    }

    @KafkaListener(topics = KafkaConfig.OUT_OF_STOCK_TOPIC,
            groupId = "admin-alert-group")
    public void handleOutOfStock(ProductEvent event) {
        log.error(">>> ADMIN ALERT: {} is OUT OF STOCK!",
                event.getProductName());
        log.error(">>> ADMIN ALERT: Product {} unavailable for purchase!",
                event.getProductId());
    }

    @KafkaListener(topics = KafkaConfig.PRODUCT_CREATED_TOPIC,
            groupId = "search-index-group")
    public void handleProductCreated(ProductEvent event) {
        log.info(">>> SEARCH INDEX: Indexing new product {}",
                event.getProductName());
        log.info(">>> SEARCH INDEX: Product {} added to search index",
                event.getProductId());
    }

    @KafkaListener(topics = KafkaConfig.PRODUCT_UPDATED_TOPIC,
            groupId = "search-index-group")
    public void handleProductUpdated(ProductEvent event) {
        log.info(">>> SEARCH INDEX: Updating index for product {}",
                event.getProductName());
        log.info(">>> CACHE SERVICE: Invalidating cache for product {}",
                event.getProductId());
    }
}