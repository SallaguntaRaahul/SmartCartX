package com.smartcart.smartcart.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEvent {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String eventType;
    private LocalDateTime timestamp;
}