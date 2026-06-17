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

        String contextItems = cart.getItems().stream()
                .map(item -> item.getProductName()
                        + " - $" + item.getProductPrice())
                .collect(Collectors.joining(", "));

        String contextSource = "cart";

        if (contextItems.isBlank()) {
            Page<OrderDTO> recentOrders = orderService
                    .getMyOrders(email, 0, 1);

            if (!recentOrders.isEmpty()) {
                OrderDTO lastOrder = recentOrders
                        .getContent().get(0);
                contextItems = lastOrder.getItems().stream()
                        .map(item -> item.getProductName()
                                + " - $" + item.getPriceAtPurchase())
                        .collect(Collectors.joining(", "));
                contextSource = "recent order #"
                        + lastOrder.getId();
            } else {
                contextSource = "none";
            }
        }

        Page<ProductDTO> products = productService
                .getAllProducts(0, 10, "id");

        String availableProducts = products.getContent()
                .stream()
                .map(p -> p.getName()
                        + " $" + p.getPrice()
                        + " - " + p.getCategory())
                .collect(Collectors.joining("\n"));

        Map<String, String> response = new HashMap<>();

        if (contextSource.equals("none")) {
            response.put("cartItems", "");
            response.put("contextSource", "none");
            response.put("recommendations",
                    "No cart items or order history found yet. "
                    + "Add items to your cart or place an order "
                    + "to get personalized recommendations.");
            return ResponseEntity.ok(response);
        }

        String recommendations = aiService
                .getProductRecommendations(
                        contextItems, availableProducts);

        response.put("cartItems", contextItems);
        response.put("contextSource", contextSource);
        response.put("recommendations", recommendations);
        return ResponseEntity.ok(response);
    }
}
