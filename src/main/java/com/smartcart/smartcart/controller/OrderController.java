package com.smartcart.smartcart.controller;

import com.smartcart.smartcart.dto.OrderDTO;
import com.smartcart.smartcart.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDTO> placeOrder(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(orderService.placeOrder(email));
    }
    @GetMapping("/all")
    public ResponseEntity<Page<OrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                orderService.getAllOrders(page, size));
    }

    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getMyOrders(
            @AuthenticationPrincipal String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                orderService.getMyOrders(email, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(
            @AuthenticationPrincipal String email,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                orderService.getOrderById(email, id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(
            @AuthenticationPrincipal String email,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                orderService.cancelOrder(email, id));
    }
}