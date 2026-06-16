package com.smartcart.smartcart.service;

import com.smartcart.smartcart.dto.*;
import com.smartcart.smartcart.kafka.UserEventProducer;
import com.smartcart.smartcart.model.User;
import com.smartcart.smartcart.repository.UserRepository;
import com.smartcart.smartcart.security.JwtUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserEventProducer userEventProducer;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("Raahul")
                .lastName("Sallagunta")
                .email("raahul@test.com")
                .password("encodedPassword")
                .role(User.Role.CUSTOMER)
                .build();
    }

    @Test
    @DisplayName("Register - Success")
    void register_Success() {
        RegisterRequest request = new RegisterRequest(
                "Raahul", "Sallagunta",
                "raahul@test.com", "password123");

        when(userRepository.existsByEmail("raahul@test.com"))
                .thenReturn(false);
        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);
        when(jwtUtil.generateToken(
                "raahul@test.com", "CUSTOMER"))
                .thenReturn("mockToken");
        doNothing().when(userEventProducer)
                .publishUserRegistered(any());

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mockToken");
        assertThat(response.getEmail())
                .isEqualTo("raahul@test.com");
        assertThat(response.getRole()).isEqualTo("CUSTOMER");

        verify(userRepository).save(any(User.class));
        verify(userEventProducer).publishUserRegistered(any());
    }

    @Test
    @DisplayName("Register - Email Already Exists")
    void register_EmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "Raahul", "Sallagunta",
                "raahul@test.com", "password123");

        when(userRepository.existsByEmail("raahul@test.com"))
                .thenReturn(true);

        assertThatThrownBy(() ->
                authService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login - Success")
    void login_Success() {
        LoginRequest request = new LoginRequest(
                "raahul@test.com", "password123");

        when(userRepository.findByEmail("raahul@test.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(
                "password123", "encodedPassword"))
                .thenReturn(true);
        when(jwtUtil.generateToken(
                "raahul@test.com", "CUSTOMER"))
                .thenReturn("mockToken");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mockToken");
        assertThat(response.getEmail())
                .isEqualTo("raahul@test.com");
        assertThat(response.getFirstName()).isEqualTo("Raahul");

        verify(jwtUtil).generateToken(
                "raahul@test.com", "CUSTOMER");
    }

    @Test
    @DisplayName("Login - User Not Found")
    void login_UserNotFound() {
        LoginRequest request = new LoginRequest(
                "unknown@test.com", "password123");

        when(userRepository.findByEmail("unknown@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.login(request))
                .isInstanceOf(RuntimeException.class);

        verify(jwtUtil, never()).generateToken(any(), any());
    }

    @Test
    @DisplayName("Login - Wrong Password")
    void login_WrongPassword() {
        LoginRequest request = new LoginRequest(
                "raahul@test.com", "wrongPassword");

        when(userRepository.findByEmail("raahul@test.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(
                "wrongPassword", "encodedPassword"))
                .thenReturn(false);

        assertThatThrownBy(() ->
                authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid password");

        verify(jwtUtil, never()).generateToken(any(), any());
    }
}