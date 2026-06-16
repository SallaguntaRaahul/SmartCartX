package com.smartcart.smartcart.security;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "smartcartSecretKey2024VeryLong" +
                        "SecretKeyForHMACSHA256Algorithm");
        ReflectionTestUtils.setField(
                jwtUtil, "expiration", 86400000L);
    }

    @Test
    @DisplayName("Generate token - not null")
    void generateToken_NotNull() {
        String token = jwtUtil.generateToken(
                "raahul@test.com", "CUSTOMER");

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("Generate token - has 3 parts")
    void generateToken_HasThreeParts() {
        String token = jwtUtil.generateToken(
                "raahul@test.com", "CUSTOMER");

        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Extract email from token")
    void extractEmail_Success() {
        String token = jwtUtil.generateToken(
                "raahul@test.com", "CUSTOMER");

        String email = jwtUtil.extractEmail(token);

        assertThat(email).isEqualTo("raahul@test.com");
    }

    @Test
    @DisplayName("Extract role from customer token")
    void extractRole_Customer() {
        String token = jwtUtil.generateToken(
                "raahul@test.com", "CUSTOMER");

        String role = jwtUtil.extractRole(token);

        assertThat(role).isEqualTo("CUSTOMER");
    }

    @Test
    @DisplayName("Extract role from admin token")
    void extractRole_Admin() {
        String token = jwtUtil.generateToken(
                "admin@test.com", "ADMIN");

        String role = jwtUtil.extractRole(token);

        assertThat(role).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Valid token returns true")
    void isTokenValid_ValidToken() {
        String token = jwtUtil.generateToken(
                "raahul@test.com", "CUSTOMER");

        boolean isValid = jwtUtil.isTokenValid(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Invalid token returns false")
    void isTokenValid_InvalidToken() {
        boolean isValid = jwtUtil.isTokenValid(
                "invalid.token.here");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Malformed token returns false")
    void isTokenValid_MalformedToken() {
        boolean isValid = jwtUtil.isTokenValid(
                "abc.def.ghi");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Different emails generate different tokens")
    void differentEmails_DifferentTokens() {
        String token1 = jwtUtil.generateToken(
                "user1@test.com", "CUSTOMER");
        String token2 = jwtUtil.generateToken(
                "user2@test.com", "CUSTOMER");

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("Admin email extracted correctly")
    void adminEmail_ExtractedCorrectly() {
        String token = jwtUtil.generateToken(
                "admin@test.com", "ADMIN");

        assertThat(jwtUtil.extractEmail(token))
                .isEqualTo("admin@test.com");
        assertThat(jwtUtil.extractRole(token))
                .isEqualTo("ADMIN");
    }
}