package com.smartcart.smartcart.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationEvent {
    private String to;
    private String subject;
    private String message;
    private String type;
    private LocalDateTime timestamp;
}