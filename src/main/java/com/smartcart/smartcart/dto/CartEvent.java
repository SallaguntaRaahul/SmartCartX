package com.smartcart.smartcart.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartEvent {
    private Long cartId;
    private Long userId;
    private String customerEmail;
    private List<CartItemDTO> items;
    private BigDecimal totalPrice;
    private String eventType;
    private LocalDateTime timestamp;
}