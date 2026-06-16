package com.smartcart.smartcart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcart.smartcart.config.JacksonConfig;
import com.smartcart.smartcart.dto.*;
import com.smartcart.smartcart.security.*;
import com.smartcart.smartcart.service.AuthService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(JacksonConfig.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private AuthService authService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private JwtAuthFilter jwtAuthFilter;

    private AuthResponse testAuthResponse;

    @BeforeEach
    void setUp() {
        testAuthResponse = AuthResponse.builder()
                .token("mockJwtToken")
                .email("raahul@test.com")
                .firstName("Raahul")
                .role("CUSTOMER")
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/register - success")
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Raahul", "Sallagunta",
                "raahul@test.com", "password123");

        when(authService.register(any()))
                .thenReturn(testAuthResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token")
                        .value("mockJwtToken"))
                .andExpect(jsonPath("$.email")
                        .value("raahul@test.com"))
                .andExpect(jsonPath("$.role")
                        .value("CUSTOMER"));

        verify(authService).register(any());
    }

    @Test
    @DisplayName("POST /api/auth/login - success")
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest(
                "raahul@test.com", "password123");

        when(authService.login(any()))
                .thenReturn(testAuthResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token")
                        .value("mockJwtToken"))
                .andExpect(jsonPath("$.firstName")
                        .value("Raahul"));

        verify(authService).login(any());
    }

    @Test
    @DisplayName("POST /api/auth/register - validation fails")
    void register_ValidationFails() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest(
                "", "", "invalid-email", "123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Validation Failed"));
    }

    @Test
    @DisplayName("POST /api/auth/login - validation fails")
    void login_ValidationFails() throws Exception {
        LoginRequest invalidRequest = new LoginRequest(
                "", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}