package com.smartcart.smartcart.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEvent {
    private Long productId;
    private String productName;
    private String category;
    private BigDecimal price;
    private Integer stockQuantity;
    private String eventType;
    private LocalDateTime timestamp;
}