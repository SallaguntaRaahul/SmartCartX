package com.smartcart.smartcart.service;

import com.smartcart.smartcart.dto.*;
import com.smartcart.smartcart.exception.ResourceNotFoundException;
import com.smartcart.smartcart.model.*;
import com.smartcart.smartcart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartDTO getCart(String email) {
        User user = getUser(email);
        Cart cart = getOrCreateCart(user);
        return toDTO(cart);
    }

    @Transactional
    public CartDTO addItem(String email, CartItemRequest request) {
        User user = getUser(email);
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product not found: " + request.getProductId()));

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock. Available: "
                    + product.getStockQuantity());
        }

        // Check if product already in cart
        cartItemRepository.findByCartIdAndProductId(
                        cart.getId(), product.getId())
                .ifPresentOrElse(
                        existingItem -> existingItem.setQuantity(
                                existingItem.getQuantity() + request.getQuantity()),
                        () -> {
                            CartItem newItem = CartItem.builder()
                                    .cart(cart)
                                    .product(product)
                                    .quantity(request.getQuantity())
                                    .build();
                            cart.getItems().add(newItem);
                        }
                );

        return toDTO(cartRepository.save(cart));
    }

    @Transactional
    public CartDTO updateItem(String email, Long itemId, Integer quantity) {
        User user = getUser(email);
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found: " + itemId));

        if (quantity <= 0) {
            cart.getItems().remove(item);
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
        }

        return toDTO(cartRepository.save(cart));
    }

    @Transactional
    public CartDTO removeItem(String email, Long itemId) {
        User user = getUser(email);
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cart item not found: " + itemId));

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return toDTO(cartRepository.save(cart));
    }

    @Transactional
    public void clearCart(String email) {
        User user = getUser(email);
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + email));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();
                    newCart.setItems(new java.util.ArrayList<>());
                    return cartRepository.save(newCart);
                });
    }

    private CartDTO toDTO(Cart cart) {
        var items = cart.getItems().stream()
                .map(item -> CartItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productPrice(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return CartDTO.builder()
                .id(cart.getId())
                .items(items)
                .totalPrice(cart.getTotalPrice())
                .totalItems(items.stream()
                        .mapToInt(CartItemDTO::getQuantity).sum())
                .build();
    }
}