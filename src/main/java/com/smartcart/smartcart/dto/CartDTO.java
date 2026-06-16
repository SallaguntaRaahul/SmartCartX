package com.smartcart.smartcart.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDTO {
    private Long id;
    private List<CartItemDTO> items;
    private BigDecimal totalPrice;
    private Integer totalItems;
}