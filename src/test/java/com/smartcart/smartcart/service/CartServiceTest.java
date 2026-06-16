package com.smartcart.smartcart.service;

import com.smartcart.smartcart.dto.*;
import com.smartcart.smartcart.exception.ResourceNotFoundException;
import com.smartcart.smartcart.model.*;
import com.smartcart.smartcart.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartService Tests")
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("raahul@test.com")
                .firstName("Raahul")
                .role(User.Role.CUSTOMER)
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("iPhone 15 Pro")
                .price(new BigDecimal("999.99"))
                .stockQuantity(50)
                .build();

        testCart = Cart.builder()
                .id(1L)
                .user(testUser)
                .items(new ArrayList<>())
                .build();

        testCartItem = CartItem.builder()
                .id(1L)
                .cart(testCart)
                .product(testProduct)
                .quantity(2)
                .build();
    }

    @Test
    @DisplayName("Get cart - existing cart")
    void getCart_ExistingCart() {
        when(userRepository.findByEmail("raahul@test.com"))
                .thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(testCart));

        CartDTO result = cartService.getCart("raahul@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getItems()).isEmpty();
        verify(cartRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("Get cart - creates new cart if not exists")
    void getCart_CreatesNewCart() {
        when(userRepository.findByEmail("raahul@test.com"))
                .thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(testCart);

        CartDTO result = cartService.getCart("raahul@test.com");

        assertThat(result).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Add item to cart - Success")
    void addItem_Success() {
        CartItemRequest request = new CartItemRequest(1L, 2);
        testCart.getItems().add(testCartItem);

        when(userRepository.findByEmail("raahul@test.com"))
                .thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L))
                .thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(testCart);

        CartDTO result = cartService.addItem(
                "raahul@test.com", request);

        assertThat(result).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Add item - Product not found")
    void addItem_ProductNotFound() {
        CartItemRequest request = new CartItemRequest(999L, 2);

        when(userRepository.findByEmail("raahul@test.com"))
                .thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(testCart));
        when(productRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                cartService.addItem("raahul@test.com", request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Add item - Insufficient stock")
    void addItem_InsufficientStock() {
        testProduct.setStockQuantity(1);
        CartItemRequest request = new CartItemRequest(1L, 10);

        when(userRepository.findByEmail("raahul@test.com"))
                .thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(testProduct));

        assertThatThrownBy(() ->
                cartService.addItem("raahul@test.com", request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("Remove item from cart - Success")
    void removeItem_Success() {
        testCart.getItems().add(testCartItem);

        when(userRepository.findByEmail("raahul@test.com"))
                .thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(1L))
                .thenReturn(Optional.of(testCartItem));
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(testCart);
        doNothing().when(cartItemRepository)
                .delete(testCartItem);

        CartDTO result = cartService.removeItem(
                "raahul@test.com", 1L);

        assertThat(result).isNotNull();
        verify(cartItemRepository).delete(testCartItem);
    }

    @Test
    @DisplayName("Clear cart - Success")
    void clearCart_Success() {
        testCart.getItems().add(testCartItem);

        when(userRepository.findByEmail("raahul@test.com"))
                .thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(testCart);

        assertThatCode(() ->
                cartService.clearCart("raahul@test.com"))
                .doesNotThrowAnyException();

        verify(cartRepository).save(any(Cart.class));
    }


    @Test
    @DisplayName("Add item - updates quantity if already in cart")
    void addItem_UpdatesExistingItem() {
        CartItemRequest request = new CartItemRequest(1L, 3);
        testCart.getItems().add(testCartItem);

        when(userRepository.findByEmail("raahul@test.com"))
                .thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(
                1L, 1L))
                .thenReturn(Optional.of(testCartItem));
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(testCart);

        CartDTO result = cartService.addItem(
                "raahul@test.com", request);

        assertThat(result).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Update item quantity - success")
    void updateItem_Success() {
        testCart.getItems().add(testCartItem);

        when(userRepository.findByEmail("raahul@test.com"))
                .thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(testCart));
        when(cartItemRepository.findById(1L))
                .thenReturn(Optional.of(testCartItem));
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(testCart);

        CartDTO result = cartService.updateItem(
                "raahul@test.com", 1L, 5);

        assertThat(result).isNotNull();
        verify(cartRepository).save(any(Cart.class));
    }
}