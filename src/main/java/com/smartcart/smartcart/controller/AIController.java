package com.smartcart.smartcart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcart.smartcart.dto.*;
import com.smartcart.smartcart.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AIController {

    private final AIService aiService;
    private final OrderService orderService;
    private final CartService cartService;
    private final ProductService productService;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
    public ResponseEntity<Map<String, Object>> getRecommendations(
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

        Map<String, Object> response = new HashMap<>();

        if (contextSource.equals("none")) {
            response.put("cartItems", "");
            response.put("contextSource", "none");
            response.put("reason",
                    "No cart items or order history found yet. "
                    + "Add items to your cart or place an order "
                    + "to get personalized recommendations.");
            response.put("products", new ArrayList<ProductDTO>());
            return ResponseEntity.ok(response);
        }

        Page<ProductDTO> productPage = productService
                .getAllProducts(0, 30, "id");
        List<ProductDTO> candidateProducts = productPage.getContent();

        String availableProducts = candidateProducts
                .stream()
                .map(p -> p.getId() + " | " + p.getName()
                        + " | $" + p.getPrice()
                        + " | " + p.getCategory())
                .collect(Collectors.joining("\n"));

        String aiRaw = aiService.getProductRecommendations(
                contextItems, availableProducts);

        String reason = "Here are some products you might like.";
        List<ProductDTO> recommendedProducts = new ArrayList<>();

        try {
            Matcher matcher = Pattern.compile("\\{[\\s\\S]*\\}")
                    .matcher(aiRaw);
            if (matcher.find()) {
                Map<?, ?> parsed = objectMapper.readValue(
                        matcher.group(), Map.class);
                Object reasonObj = parsed.get("reason");
                if (reasonObj != null) {
                    reason = reasonObj.toString();
                }
                List<?> ids = (List<?>) parsed.get("productIds");
                if (ids != null) {
                    for (Object idObj : ids) {
                        Long id = Long.valueOf(idObj.toString());
                        candidateProducts.stream()
                                .filter(p -> p.getId().equals(id))
                                .findFirst()
                                .ifPresent(recommendedProducts::add);
                    }
                }
            }
        } catch (Exception e) {
            log.error(">>> Failed to parse AI recommendation "
                    + "JSON: {}", e.getMessage());
        }

        if (recommendedProducts.isEmpty()) {
            recommendedProducts = candidateProducts.stream()
                    .limit(3)
                    .collect(Collectors.toList());
            reason = "Popular picks from our catalog.";
        }

        response.put("cartItems", contextItems);
        response.put("contextSource", contextSource);
        response.put("reason", reason);
        response.put("products", recommendedProducts);
        return ResponseEntity.ok(response);
    }
}
