package com.smartcart.smartcart.kafka;

import com.smartcart.smartcart.config.KafkaConfig;
import com.smartcart.smartcart.dto.OrderEvent;
import com.smartcart.smartcart.model.Order;
import com.smartcart.smartcart.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = KafkaConfig.ORDER_PLACED_TOPIC,
            groupId = "order-status-group")
    public void handleOrderPlaced(OrderEvent event) {
        log.info(">>> ORDER STATUS SERVICE: Updating order {} " +
                "to CONFIRMED", event.getOrderId());

        orderRepository.findById(event.getOrderId())
                .ifPresent(order -> {
                    order.setStatus(Order.OrderStatus.CONFIRMED);
                    orderRepository.save(order);
                    log.info(">>> ORDER STATUS SERVICE: Order {} " +
                                    "status updated to CONFIRMED",
                            event.getOrderId());
                });
    }
}