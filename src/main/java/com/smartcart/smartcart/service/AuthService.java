package com.smartcart.smartcart.service;

import com.smartcart.smartcart.dto.*;
import com.smartcart.smartcart.exception.ResourceNotFoundException;
import com.smartcart.smartcart.kafka.UserEventProducer;
import com.smartcart.smartcart.model.User;
import com.smartcart.smartcart.repository.UserRepository;
import com.smartcart.smartcart.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserEventProducer userEventProducer;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.CUSTOMER)
                .build();

        userRepository.save(user);

        // Publish USER_REGISTERED Kafka event
        userEventProducer.publishUserRegistered(UserEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .eventType("USER_REGISTERED")
                .timestamp(LocalDateTime.now())
                .build());

        String token = jwtUtil.generateToken(
                user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .role(user.getRole().name())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"));

        if (!passwordEncoder.matches(request.getPassword(),
                user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(
                user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .role(user.getRole().name())
                .build();
    }
}