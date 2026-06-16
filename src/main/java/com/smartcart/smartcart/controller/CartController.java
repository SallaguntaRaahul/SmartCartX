package com.smartcart.smartcart.controller;

import com.smartcart.smartcart.dto.*;
import com.smartcart.smartcart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDTO> getCart(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(cartService.getCart(email));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItem(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(email, request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> updateItem(
            @AuthenticationPrincipal String email,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateItem(email, itemId, quantity));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> removeItem(
            @AuthenticationPrincipal String email,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(email, itemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal String email) {
        cartService.clearCart(email);
        return ResponseEntity.noContent().build();
    }
}