package com.smartcart.smartcart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcart.smartcart.dto.OrderDTO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";
    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    private String callGroq(String prompt) {
        try {
            log.info(">>> Calling Groq API, key starts with: {}",
                    apiKey.substring(0, 10));

            Map<String, Object> requestBody = Map.of(
                    "model", "llama-3.1-8b-instant",
                    "max_tokens", 500,
                    "messages", List.of(
                            Map.of("role", "user",
                                    "content", prompt)
                    )
            );

            String json = objectMapper
                    .writeValueAsString(requestBody);

            Request request = new Request.Builder()
                    .url(GROQ_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(json, JSON))
                    .build();

            try (Response response = client
                    .newCall(request).execute()) {
                int code = response.code();
                String responseBody = response.body().string();
                log.info(">>> Groq response code: {}", code);
                log.info(">>> Groq response body: {}", responseBody);

                if (code != 200) {
                    return "AI error: " + responseBody;
                }

                Map<?, ?> parsed = objectMapper
                        .readValue(responseBody, Map.class);
                List<?> choices = (List<?>) parsed.get("choices");
                Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
                Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
                return (String) message.get("content");
            }
        } catch (Exception e) {
            log.error(">>> Groq API error: {}", e.getMessage(), e);
            return "AI error: " + e.getMessage();
        }
    }

    public String generateOrderSummary(OrderDTO order) {
        String itemsList = order.getItems().stream()
                .map(item -> String.format(
                        "%s x%d at $%.2f",
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPriceAtPurchase()))
                .collect(Collectors.joining(", "));

        String prompt = String.format(
                "You are an e-commerce assistant for SmartCartX. " +
                "Generate a friendly concise order confirmation " +
                "summary in 2-3 sentences. " +
                "Order ID: %d, Status: %s, Items: %s, Total: $%.2f",
                order.getId(),
                order.getStatus(),
                itemsList,
                order.getTotalAmount());

        return callGroq(prompt);
    }

    public String detectAnomalies(OrderDTO order) {
        String itemsList = order.getItems().stream()
                .map(item -> String.format(
                        "%s x%d at $%.2f",
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPriceAtPurchase()))
                .collect(Collectors.joining(", "));

        String prompt = String.format(
                "You are a fraud detection AI for SmartCartX. " +
                "Analyze this order for anomalies. " +
                "Order ID: %d, Items: %s, Total: $%.2f, Count: %d. " +
                "Respond ONLY in JSON: " +
                "{\"isAnomalous\": false, \"riskLevel\": \"LOW\", " +
                "\"reason\": \"explanation\"}",
                order.getId(),
                itemsList,
                order.getTotalAmount(),
                order.getItems().size());

        return callGroq(prompt);
    }

    public String getProductRecommendations(
            String cartItems,
            String availableProducts) {

        String prompt = String.format(
                "You are a product recommendation AI for SmartCartX. " +
                "Based on the customer context: %s, " +
                "recommend exactly 3 products from this list " +
                "(format: ID | Name | Price | Category):\n%s\n\n" +
                "Respond ONLY with valid JSON, no other text, " +
                "in this exact format: " +
                "{\"reason\": \"one or two sentence " +
                "explanation of why these were picked\", " +
                "\"productIds\": [id1, id2, id3]}",
                cartItems,
                availableProducts);

        return callGroq(prompt);
    }
}
