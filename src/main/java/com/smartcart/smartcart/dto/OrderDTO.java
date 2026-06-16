package com.smartcart.smartcart.dto;

import com.smartcart.smartcart.model.Order;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private Long id;
    private String status;
    private BigDecimal totalAmount;
    private List<OrderItemDTO> items;
    private LocalDateTime createdAt;
}