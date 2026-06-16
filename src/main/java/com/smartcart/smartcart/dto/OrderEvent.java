package com.smartcart.smartcart.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent {

    private Long orderId;
    private String customerEmail;
    private String status;
    private BigDecimal totalAmount;
    private List<OrderItemDTO> items;
    private LocalDateTime timestamp;
}