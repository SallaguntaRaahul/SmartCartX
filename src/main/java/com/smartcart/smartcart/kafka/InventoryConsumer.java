package com.smartcart.smartcart.kafka;

import com.smartcart.smartcart.config.KafkaConfig;
import com.smartcart.smartcart.dto.OrderEvent;
import com.smartcart.smartcart.dto.ProductEvent;
import com.smartcart.smartcart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryConsumer {

    private final ProductRepository productRepository;
    private final ProductEventProducer productEventProducer;
    private final CacheManager cacheManager;

    private static final int LOW_STOCK_THRESHOLD = 5;

    @KafkaListener(
            topics = KafkaConfig.ORDER_PLACED_TOPIC,
            groupId = "inventory-group")
    public void handleOrderPlaced(OrderEvent event) {
        log.info(">>> INVENTORY SERVICE: Processing order {}",
                event.getOrderId());

        event.getItems().forEach(item -> {
            productRepository.findById(item.getProductId())
                    .ifPresent(product -> {
                        int previousStock = product.getStockQuantity();
                        int newStock = Math.max(0,
                                previousStock - item.getQuantity());

                        product.setStockQuantity(newStock);
                        productRepository.save(product);

                        log.info(">>> INVENTORY SERVICE: {} stock " +
                                        "reduced from {} to {}",
                                product.getName(),
                                previousStock,
                                newStock);

                        // Evict Redis cache so next GET shows
                        // updated stock
                        if (cacheManager.getCache("products") != null) {
                            cacheManager.getCache("products")
                                    .evict(product.getId());
                            log.info(">>> INVENTORY SERVICE: " +
                                            "Cache evicted for product {}",
                                    product.getId());
                        }

                        // Check low stock
                        if (newStock <= LOW_STOCK_THRESHOLD
                                && newStock > 0) {
                            productEventProducer.publishLowStockAlert(
                                    ProductEvent.builder()
                                            .productId(product.getId())
                                            .productName(product.getName())
                                            .stockQuantity(newStock)
                                            .eventType("LOW_STOCK")
                                            .timestamp(LocalDateTime.now())
                                            .build());
                        }

                        // Check out of stock
                        if (newStock == 0) {
                            log.error(">>> INVENTORY SERVICE: " +
                                            "{} is now OUT OF STOCK!",
                                    product.getName());
                            productEventProducer.publishOutOfStock(
                                    ProductEvent.builder()
                                            .productId(product.getId())
                                            .productName(product.getName())
                                            .stockQuantity(0)
                                            .eventType("OUT_OF_STOCK")
                                            .timestamp(LocalDateTime.now())
                                            .build());
                        }
                    });
        });

        log.info(">>> INVENTORY SERVICE: Stock updated for order {}",
                event.getOrderId());
    }
}