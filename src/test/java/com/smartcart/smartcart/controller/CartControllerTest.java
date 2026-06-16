package com.smartcart.smartcart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcart.smartcart.config.JacksonConfig;
import com.smartcart.smartcart.dto.*;
import com.smartcart.smartcart.security.*;
import com.smartcart.smartcart.service.CartService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(JacksonConfig.class)
@DisplayName("CartController Tests")
class CartControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private CartService cartService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private JwtAuthFilter jwtAuthFilter;

    private CartDTO testCartDTO;

    private void setAuth(String email) {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                email, null,
                                List.of(new SimpleGrantedAuthority(
                                        "ROLE_CUSTOMER"))));
    }

    @BeforeEach
    void setUp() {
        CartItemDTO item = CartItemDTO.builder()
                .id(1L)
                .productId(1L)
                .productName("iPhone 15 Pro")
                .productPrice(new BigDecimal("999.99"))
                .quantity(2)
                .subtotal(new BigDecimal("1999.98"))
                .build();

        testCartDTO = CartDTO.builder()
                .id(1L)
                .items(List.of(item))
                .totalPrice(new BigDecimal("1999.98"))
                .totalItems(2)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/cart - returns cart")
    void getCart_Success() throws Exception {
        setAuth("raahul@test.com");
        when(cartService.getCart("raahul@test.com"))
                .thenReturn(testCartDTO);

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.items").isArray());
    }

    @Test
    @DisplayName("POST /api/cart/items - adds item")
    void addItem_Success() throws Exception {
        setAuth("raahul@test.com");
        CartItemRequest request = new CartItemRequest(1L, 2);

        when(cartService.addItem(
                eq("raahul@test.com"), any()))
                .thenReturn(testCartDTO);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productName")
                        .value("iPhone 15 Pro"));
    }

    @Test
    @DisplayName("PUT /api/cart/items/{id} - updates quantity")
    void updateItem_Success() throws Exception {
        setAuth("raahul@test.com");
        when(cartService.updateItem(
                eq("raahul@test.com"), eq(1L), eq(5)))
                .thenReturn(testCartDTO);

        mockMvc.perform(put("/api/cart/items/1")
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("DELETE /api/cart/items/{id} - removes item")
    void removeItem_Success() throws Exception {
        setAuth("raahul@test.com");
        when(cartService.removeItem(
                eq("raahul@test.com"), eq(1L)))
                .thenReturn(testCartDTO);

        mockMvc.perform(delete("/api/cart/items/1"))
                .andExpect(status().isOk());

        verify(cartService).removeItem(
                "raahul@test.com", 1L);
    }

    @Test
    @DisplayName("DELETE /api/cart - clears cart")
    void clearCart_Success() throws Exception {
        setAuth("raahul@test.com");
        doNothing().when(cartService)
                .clearCart("raahul@test.com");

        mockMvc.perform(delete("/api/cart"))
                .andExpect(status().isNoContent());

        verify(cartService).clearCart("raahul@test.com");
    }

    @Test
    @DisplayName("POST /api/cart/items - validation fails")
    void addItem_ValidationFails() throws Exception {
        setAuth("raahul@test.com");
        CartItemRequest invalidRequest =
                new CartItemRequest(null, 0);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Validation Failed"));
    }
}