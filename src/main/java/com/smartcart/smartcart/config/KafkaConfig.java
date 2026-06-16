package com.smartcart.smartcart.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    // Order topics
    public static final String ORDER_PLACED_TOPIC = "order-placed";
    public static final String ORDER_CONFIRMED_TOPIC = "order-confirmed";
    public static final String ORDER_CANCELLED_TOPIC = "order-cancelled";

    // Product topics
    public static final String PRODUCT_CREATED_TOPIC = "product-created";
    public static final String PRODUCT_UPDATED_TOPIC = "product-updated";
    public static final String LOW_STOCK_ALERT_TOPIC = "low-stock-alert";
    public static final String OUT_OF_STOCK_TOPIC = "out-of-stock";

    // User topics
    public static final String USER_REGISTERED_TOPIC = "user-registered";

    // Cart topics
    public static final String CART_ABANDONED_TOPIC = "cart-abandoned";

    // Notification topic
    public static final String NOTIFICATION_TOPIC = "notification";

    // Dead Letter Queue
    public static final String ORDER_PLACED_DLT = "order-placed.DLT";

    @Bean public NewTopic orderPlacedTopic() {
        return TopicBuilder.name(ORDER_PLACED_TOPIC)
                .partitions(3).replicas(1).build();
    }

    @Bean public NewTopic orderConfirmedTopic() {
        return TopicBuilder.name(ORDER_CONFIRMED_TOPIC)
                .partitions(3).replicas(1).build();
    }

    @Bean public NewTopic orderCancelledTopic() {
        return TopicBuilder.name(ORDER_CANCELLED_TOPIC)
                .partitions(3).replicas(1).build();
    }

    @Bean public NewTopic productCreatedTopic() {
        return TopicBuilder.name(PRODUCT_CREATED_TOPIC)
                .partitions(3).replicas(1).build();
    }

    @Bean public NewTopic productUpdatedTopic() {
        return TopicBuilder.name(PRODUCT_UPDATED_TOPIC)
                .partitions(3).replicas(1).build();
    }

    @Bean public NewTopic lowStockAlertTopic() {
        return TopicBuilder.name(LOW_STOCK_ALERT_TOPIC)
                .partitions(1).replicas(1).build();
    }

    @Bean public NewTopic outOfStockTopic() {
        return TopicBuilder.name(OUT_OF_STOCK_TOPIC)
                .partitions(1).replicas(1).build();
    }

    @Bean public NewTopic userRegisteredTopic() {
        return TopicBuilder.name(USER_REGISTERED_TOPIC)
                .partitions(3).replicas(1).build();
    }

    @Bean public NewTopic cartAbandonedTopic() {
        return TopicBuilder.name(CART_ABANDONED_TOPIC)
                .partitions(3).replicas(1).build();
    }

    @Bean public NewTopic notificationTopic() {
        return TopicBuilder.name(NOTIFICATION_TOPIC)
                .partitions(3).replicas(1).build();
    }

    @Bean public NewTopic orderPlacedDLT() {
        return TopicBuilder.name(ORDER_PLACED_DLT)
                .partitions(1).replicas(1).build();
    }
}