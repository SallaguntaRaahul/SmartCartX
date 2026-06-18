package com.smartcart.smartcart.service;

import com.smartcart.smartcart.dto.*;
import com.smartcart.smartcart.exception.ResourceNotFoundException;
import com.smartcart.smartcart.kafka.OrderEventProducer;
import com.smartcart.smartcart.model.*;
import com.smartcart.smartcart.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final OrderEventProducer orderEventProducer;
    private final ProductRepository productRepository;
    private final CacheManager cacheManager;

    @Transactional
    public OrderDTO placeOrder(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException(
                    "Cannot place order with empty cart");
        }

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> OrderItem.builder()
                        .product(cartItem.getProduct())
                        .quantity(cartItem.getQuantity())
                        .priceAtPurchase(
                                cartItem.getProduct().getPrice())
                        .build())
                .collect(Collectors.toList());

        BigDecimal total = orderItems.stream()
                .map(item -> item.getPriceAtPurchase()
                        .multiply(BigDecimal.valueOf(
                                item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .user(user)
                .status(Order.OrderStatus.PENDING)
                .totalAmount(total)
                .build();

        orderItems.forEach(item -> item.setOrder(order));
        order.setItems(orderItems);

        Order saved = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        OrderDTO orderDTO = toDTO(saved);

        OrderEvent event = OrderEvent.builder()
                .orderId(saved.getId())
                .customerEmail(user.getEmail())
                .status("PLACED")
                .totalAmount(saved.getTotalAmount())
                .items(orderDTO.getItems())
                .timestamp(LocalDateTime.now())
                .build();
        orderEventProducer.publishOrderPlaced(event);

        return orderDTO;
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getMyOrders(
            String email, int page, int size) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return orderRepository
                .findByUserId(user.getId(), pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(String email, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found: " + orderId));
        return toDTO(order);
    }

    @Transactional
    public OrderDTO cancelOrder(String email, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found: " + orderId));

        // Only PENDING and CONFIRMED can be cancelled
        if (order.getStatus() == Order.OrderStatus.PROCESSING
                || order.getStatus() == Order.OrderStatus.SHIPPED
                || order.getStatus() == Order.OrderStatus.DELIVERED
                || order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException(
                    "Order cannot be cancelled. Current status: "
                            + order.getStatus()
                            + ". Only PENDING or CONFIRMED orders "
                            + "can be cancelled.");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        log.info(">>> ORDER CANCELLED: Order {} by {}",
                orderId, email);

        // Restore stock for each item
        saved.getItems().forEach(item -> {
            productRepository.findById(
                            item.getProduct().getId())
                    .ifPresent(product -> {
                        int restoredStock =
                                product.getStockQuantity()
                                        + item.getQuantity();
                        product.setStockQuantity(restoredStock);
                        productRepository.save(product);

                        log.info(">>> CANCEL: Restored {} units " +
                                        "of {} (new stock: {})",
                                item.getQuantity(),
                                product.getName(),
                                restoredStock);
                    });
        });

        // Clear entire products cache so all stock
        // values refresh from DB on next request
        try {
            org.springframework.cache.Cache cache =
                    cacheManager.getCache("products");
            if (cache != null) {
                cache.clear();
                log.info(">>> CANCEL: Products cache cleared " +
                        "after order {} cancellation", orderId);
            }
        } catch (Exception e) {
            log.warn(">>> CANCEL: Cache clear failed: {}",
                    e.getMessage());
        }

        // Publish cancellation event
        OrderEvent event = OrderEvent.builder()
                .orderId(saved.getId())
                .customerEmail(saved.getUser().getEmail())
                .status("CANCELLED")
                .totalAmount(saved.getTotalAmount())
                .items(toDTO(saved).getItems())
                .timestamp(LocalDateTime.now())
                .build();
        orderEventProducer.publishOrderCancelled(event);

        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public Page<OrderDTO> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return orderRepository.findAll(pageable)
                .map(this::toDTO);
    }

    private OrderDTO toDTO(Order order) {
        List<OrderItemDTO> items = order.getItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .category(item.getProduct().getCategory())
                        .quantity(item.getQuantity())
                        .priceAtPurchase(item.getPriceAtPurchase())
                        .subtotal(item.getPriceAtPurchase()
                                .multiply(BigDecimal.valueOf(
                                        item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(order.getId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .items(items)
                .createdAt(order.getCreatedAt())
                .build();
    }
}