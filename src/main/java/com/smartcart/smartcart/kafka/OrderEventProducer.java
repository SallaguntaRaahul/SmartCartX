package com.smartcart.smartcart.kafka;

import com.smartcart.smartcart.config.KafkaConfig;
import com.smartcart.smartcart.dto.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void publishOrderPlaced(OrderEvent event) {
        log.info(">>> Publishing ORDER_PLACED event for order: {}",
                event.getOrderId());

        CompletableFuture<SendResult<String, OrderEvent>> future =
                kafkaTemplate.send(
                        KafkaConfig.ORDER_PLACED_TOPIC,
                        event.getOrderId().toString(),
                        event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info(">>> ORDER_PLACED published successfully " +
                                "to partition: {}, offset: {}",
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error(">>> Failed to publish ORDER_PLACED: {}",
                        ex.getMessage());
            }
        });
    }

    public void publishOrderCancelled(OrderEvent event) {
        log.info(">>> Publishing ORDER_CANCELLED event for order: {}",
                event.getOrderId());
        kafkaTemplate.send(
                KafkaConfig.ORDER_CANCELLED_TOPIC,
                event.getOrderId().toString(),
                event);
    }
}