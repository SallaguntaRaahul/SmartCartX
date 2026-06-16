package com.smartcart.smartcart.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    public static final String ORDER_PLACED_TOPIC = "order-placed";
    public static final String ORDER_CONFIRMED_TOPIC = "order-confirmed";
    public static final String ORDER_CANCELLED_TOPIC = "order-cancelled";
    public static final String PRODUCT_CREATED_TOPIC = "product-created";
    public static final String PRODUCT_UPDATED_TOPIC = "product-updated";
    public static final String LOW_STOCK_ALERT_TOPIC = "low-stock-alert";
    public static final String OUT_OF_STOCK_TOPIC = "out-of-stock";
    public static final String USER_REGISTERED_TOPIC = "user-registered";
    public static final String CART_ABANDONED_TOPIC = "cart-abandoned";
    public static final String NOTIFICATION_TOPIC = "notification";

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.security.protocol:PLAINTEXT}")
    private String securityProtocol;

    @Value("${spring.kafka.properties.sasl.mechanism:PLAIN}")
    private String saslMechanism;

    @Value("${spring.kafka.properties.sasl.jaas.config:}")
    private String saslJaasConfig;

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        if (!securityProtocol.equals("PLAINTEXT")) {
            props.put("security.protocol", securityProtocol);
            props.put("sasl.mechanism", saslMechanism);
            if (!saslJaasConfig.isEmpty()) {
                props.put("sasl.jaas.config", saslJaasConfig);
            }
        }

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}