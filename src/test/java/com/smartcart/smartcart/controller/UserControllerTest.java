package com.smartcart.smartcart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcart.smartcart.config.JacksonConfig;
import com.smartcart.smartcart.dto.*;
import com.smartcart.smartcart.security.*;
import com.smartcart.smartcart.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(JacksonConfig.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private UserService userService;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private JwtAuthFilter jwtAuthFilter;

    private UserDTO testUserDTO;

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
        testUserDTO = UserDTO.builder()
                .id(1L)
                .firstName("Raahul")
                .lastName("Sallagunta")
                .email("raahul@test.com")
                .role("CUSTOMER")
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /api/users/me - returns profile")
    void getMyProfile_Success() throws Exception {
        setAuth("raahul@test.com");
        when(userService.getMyProfile("raahul@test.com"))
                .thenReturn(testUserDTO);

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email")
                        .value("raahul@test.com"))
                .andExpect(jsonPath("$.firstName")
                        .value("Raahul"))
                .andExpect(jsonPath("$.role")
                        .value("CUSTOMER"));
    }

    @Test
    @DisplayName("PUT /api/users/me - updates profile")
    void updateMyProfile_Success() throws Exception {
        setAuth("raahul@test.com");
        UpdateUserRequest request = new UpdateUserRequest(
                "Updated", "Name");

        UserDTO updated = UserDTO.builder()
                .id(1L)
                .firstName("Updated")
                .lastName("Name")
                .email("raahul@test.com")
                .role("CUSTOMER")
                .build();

        when(userService.updateMyProfile(
                "raahul@test.com", request))
                .thenReturn(updated);

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName")
                        .value("Updated"))
                .andExpect(jsonPath("$.lastName")
                        .value("Name"));
    }

    @Test
    @DisplayName("GET /api/users - admin gets all users")
    void getAllUsers_AdminSuccess() throws Exception {
        setAdminAuth();
        Page<UserDTO> page = new PageImpl<>(
                List.of(testUserDTO),
                PageRequest.of(0, 10), 1);
        when(userService.getAllUsers(anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email")
                        .value("raahul@test.com"))
                .andExpect(jsonPath("$.totalElements")
                        .value(1));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - admin deletes user")
    void deleteUser_AdminSuccess() throws Exception {
        setAdminAuth();
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    @DisplayName("PUT /api/users/me - validation fails")
    void updateProfile_ValidationFails() throws Exception {
        setAuth("raahul@test.com");
        UpdateUserRequest invalid = new UpdateUserRequest(
                "", "");

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Validation Failed"));
    }
}