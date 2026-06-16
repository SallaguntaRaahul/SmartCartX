package com.smartcart.smartcart.service;

import com.smartcart.smartcart.dto.OrderDTO;
import com.smartcart.smartcart.exception.ResourceNotFoundException;
import com.smartcart.smartcart.kafka.OrderEventProducer;
import com.smartcart.smartcart.model.Cart;
import com.smartcart.smartcart.model.CartItem;
import com.smartcart.smartcart.model.Product;
import com.smartcart.smartcart.model.User;
import com.smartcart.smartcart.model.Order;
import com.smartcart.smartcart.model.OrderItem;
import com.smartcart.smartcart.repository.CartRepository;
import com.smartcart.smartcart.repository.OrderRepository;
import com.smartcart.smartcart.repository.ProductRepository;
import com.smartcart.smartcart.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderEventProducer orderEventProducer;
    @Mock private ProductRepository productRepository;
    @Mock private CacheManager cacheManager;
    @Mock private Cache cache;

    @InjectMocks
    private OrderService orderService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private Order testOrder;

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

        CartItem cartItem = CartItem.builder()
                .id(1L)
                .product(testProduct)
                .quantity(2)
                .build();

        testCart = Cart.builder()
                .id(1L)
                .user(testUser)
                .items(new ArrayList<>(List.of(cartItem)))
                .build();
        cartItem.setCart(testCart);

        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .product(testProduct)
                .quantity(2)
                .priceAtPurchase(new BigDecimal("999.99"))
                .build();

        testOrder = Order.builder()
                .id(1L)
                .user(testUser)
                .status(Order.OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("1999.98"))
                .items(new ArrayList<>(List.of(orderItem)))
                .build();
        orderItem.setOrder(testOrder);
    }

    @Test
    @DisplayName("Place order - Success")
    void placeOrder_Success() {
        when(userRepository.findByEmail("raahul@test.com"))
                .thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(testCart));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(testOrder);
        when(cartRepository.save(any(Cart.class)))
                .thenReturn(testCart);
        doNothing().when(orderEventProducer)
                .publishOrderPlaced(any());

        OrderDTO result = orderService
                .placeOrder("raahul@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getTotalAmount())
                .isEqualByComparingTo(new BigDecimal("1999.98"));
        assertThat(result.getItems()).hasSize(1);

        verify(orderRepository).save(any(Order.class));
        verify(orderEventProducer).publishOrderPlaced(any());
    }

    @Test
    @DisplayName("Place order - Empty Cart")
    void placeOrder_EmptyCart() {
        Cart emptyCart = Cart.builder()
                .id(1L)
                .user(testUser)
                .items(new ArrayList<>())
                .build();

        when(userRepository.findByEmail("raahul@test.com"))
                .thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserId(1L))
                .thenReturn(Optional.of(emptyCart));

        assertThatThrownBy(() ->
                orderService.placeOrder("raahul@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("empty cart");

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    @DisplayName("Cancel order - CONFIRMED order Success")
    void cancelOrder_ConfirmedSuccess() {
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(testOrder);
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class)))
                .thenReturn(testProduct);
        when(cacheManager.getCache("products"))
                .thenReturn(cache);
        doNothing().when(cache).clear();
        doNothing().when(orderEventProducer)
                .publishOrderCancelled(any());

        OrderDTO result = orderService
                .cancelOrder("raahul@test.com", 1L);

        assertThat(result).isNotNull();
        verify(orderRepository).save(any(Order.class));
        verify(orderEventProducer).publishOrderCancelled(any());
    }

    @Test
    @DisplayName("Cancel order - Already Cancelled")
    void cancelOrder_AlreadyCancelled() {
        testOrder.setStatus(Order.OrderStatus.CANCELLED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() ->
                orderService.cancelOrder(
                        "raahul@test.com", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("cannot be cancelled");

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    @DisplayName("Cancel order - SHIPPED cannot cancel")
    void cancelOrder_ShippedCannotCancel() {
        testOrder.setStatus(Order.OrderStatus.SHIPPED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() ->
                orderService.cancelOrder(
                        "raahul@test.com", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("cannot be cancelled");
    }

    @Test
    @DisplayName("Get order by ID - Not Found")
    void getOrderById_NotFound() {
        when(orderRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                orderService.getOrderById(
                        "raahul@test.com", 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }


    @Test
    @DisplayName("Place order - User not found")
    void placeOrder_UserNotFound() {
        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                orderService.placeOrder("unknown@test.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Cancel order - PENDING order success")
    void cancelOrder_PendingSuccess() {
        testOrder.setStatus(Order.OrderStatus.PENDING);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(testOrder);
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class)))
                .thenReturn(testProduct);
        when(cacheManager.getCache("products"))
                .thenReturn(cache);
        doNothing().when(cache).clear();
        doNothing().when(orderEventProducer)
                .publishOrderCancelled(any());

        OrderDTO result = orderService
                .cancelOrder("raahul@test.com", 1L);

        assertThat(result).isNotNull();
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Cancel order - DELIVERED cannot cancel")
    void cancelOrder_DeliveredCannotCancel() {
        testOrder.setStatus(Order.OrderStatus.DELIVERED);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(testOrder));

        assertThatThrownBy(() ->
                orderService.cancelOrder(
                        "raahul@test.com", 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("cannot be cancelled");
    }

    @Test
    @DisplayName("Get all orders - paginated")
    void getAllOrders_Paginated() {
        Page<Order> page = new PageImpl<>(
                List.of(testOrder),
                PageRequest.of(0, 10), 1);
        when(orderRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<OrderDTO> result =
                orderService.getAllOrders(0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Get my orders - success")
    void getMyOrders_Success() {
        Page<Order> page = new PageImpl<>(
                List.of(testOrder),
                PageRequest.of(0, 10), 1);
        when(userRepository.findByEmail("raahul@test.com"))
                .thenReturn(Optional.of(testUser));
        when(orderRepository.findByUserId(
                eq(1L), any(Pageable.class)))
                .thenReturn(page);

        Page<OrderDTO> result = orderService
                .getMyOrders("raahul@test.com", 0, 10);

        assertThat(result.getContent()).hasSize(1);
    }
}