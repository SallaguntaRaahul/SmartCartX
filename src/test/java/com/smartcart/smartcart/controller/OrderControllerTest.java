package com.smartcart.smartcart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcart.smartcart.config.JacksonConfig;
import com.smartcart.smartcart.dto.*;
import com.smartcart.smartcart.security.*;
import com.smartcart.smartcart.service.OrderService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(JacksonConfig.class)
@DisplayName("OrderController Tests")
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private OrderService orderService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private JwtAuthFilter jwtAuthFilter;

    private OrderDTO testOrderDTO;

    private void setAuth(String email) {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                email, null,
                                List.of(new SimpleGrantedAuthority(
                                        "ROLE_CUSTOMER"))));
    }

    private void setAdminAuth() {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "admin@test.com", null,
                                List.of(new SimpleGrantedAuthority(
                                        "ROLE_ADMIN"))));
    }

    @BeforeEach
    void setUp() {
        testOrderDTO = OrderDTO.builder()
                .id(1L)
                .status("CONFIRMED")
                .totalAmount(new BigDecimal("999.99"))
                .items(List.of())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/orders - places order")
    void placeOrder_Success() throws Exception {
        setAuth("raahul@test.com");
        when(orderService.placeOrder("raahul@test.com"))
                .thenReturn(testOrderDTO);

        mockMvc.perform(post("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status")
                        .value("CONFIRMED"));
    }

    @Test
    @DisplayName("GET /api/orders - returns my orders")
    void getMyOrders_Success() throws Exception {
        setAuth("raahul@test.com");
        Page<OrderDTO> page = new PageImpl<>(
                List.of(testOrderDTO),
                PageRequest.of(0, 10), 1);
        when(orderService.getMyOrders(
                eq("raahul@test.com"), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status")
                        .value("CONFIRMED"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - returns order")
    void getOrderById_Success() throws Exception {
        setAuth("raahul@test.com");
        when(orderService.getOrderById(
                "raahul@test.com", 1L))
                .thenReturn(testOrderDTO);

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.totalAmount")
                        .value(999.99));
    }

    @Test
    @DisplayName("PUT /api/orders/{id}/cancel - cancels order")
    void cancelOrder_Success() throws Exception {
        setAuth("raahul@test.com");
        OrderDTO cancelledOrder = OrderDTO.builder()
                .id(1L)
                .status("CANCELLED")
                .totalAmount(new BigDecimal("999.99"))
                .items(List.of())
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.cancelOrder(
                "raahul@test.com", 1L))
                .thenReturn(cancelledOrder);

        mockMvc.perform(put("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("CANCELLED"));
    }

    @Test
    @DisplayName("GET /api/orders/all - admin gets all orders")
    void getAllOrders_AdminSuccess() throws Exception {
        setAdminAuth();
        Page<OrderDTO> page = new PageImpl<>(
                List.of(testOrderDTO),
                PageRequest.of(0, 10), 1);
        when(orderService.getAllOrders(anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/orders/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.totalElements").value(1));
    }
}