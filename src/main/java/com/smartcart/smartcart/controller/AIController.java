package com.smartcart.smartcart.controller;

import com.smartcart.smartcart.dto.*;
import com.smartcart.smartcart.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;
    private final OrderService orderService;
    private final CartService cartService;
    private final ProductService productService;

    @GetMapping("/orders/{id}/summary")
    public ResponseEntity<Map<String, String>> getOrderSummary(
            @AuthenticationPrincipal String email,
            @PathVariable Long id) {

        OrderDTO order = orderService.getOrderById(email, id);
        String summary = aiService.generateOrderSummary(order);

        Map<String, String> response = new HashMap<>();
        response.put("orderId", id.toString());
        response.put("summary", summary);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/{id}/anomaly")
    public ResponseEntity<Map<String, Object>> checkOrderAnomaly(
            @AuthenticationPrincipal String email,
            @PathVariable Long id) {

        OrderDTO order = orderService.getOrderById(email, id);
        String anomalyResult = aiService.detectAnomalies(order);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", id);
        response.put("analysis", anomalyResult);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<Map<String, String>> getRecommendations(
            @AuthenticationPrincipal String email) {

        CartDTO cart = cartService.getCart(email);

        String cartItems = cart.getItems().stream()
                .map(item -> item.getProductName()
                        + " - $" + item.getProductPrice())
                .collect(Collectors.joining(", "));

        Page<ProductDTO> products = productService
                .getAllProducts(0, 10, "id");

        String availableProducts = products.getContent()
                .stream()
                .map(p -> p.getName()
                        + " $" + p.getPrice()
                        + " - " + p.getCategory())
                .collect(Collectors.joining("\n"));

        String recommendations = aiService
                .getProductRecommendations(
                        cartItems, availableProducts);

        Map<String, String> response = new HashMap<>();
        response.put("cartItems", cartItems);
        response.put("recommendations", recommendations);
        return ResponseEntity.ok(response);
    }
}
