package com.smartcart.smartcart.exception;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Handle ResourceNotFoundException - 404")
    void handleNotFound_Returns404() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException(
                        "Product not found with id: 1");

        ResponseEntity<Map<String, Object>> response =
                handler.handleNotFound(ex);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .containsEntry("status", 404);
        assertThat(response.getBody()
                .get("message").toString())
                .contains("Product not found");
    }

    @Test
    @DisplayName("Handle RuntimeException - 400")
    void handleRuntime_Returns400() {
        RuntimeException ex = new RuntimeException(
                "Email already registered");

        ResponseEntity<Map<String, Object>> response =
                handler.handleRuntime(ex);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("status", 400);
        assertThat(response.getBody()
                .get("message").toString())
                .contains("Email already registered");
    }

    @Test
    @DisplayName("Handle ValidationException - 400 with fields")
    void handleValidation_Returns400WithFields()
            throws Exception {
        MethodArgumentNotValidException ex =
                mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult =
                mock(BindingResult.class);

        FieldError fieldError = new FieldError(
                "registerRequest",
                "email",
                "Invalid email");

        when(ex.getBindingResult())
                .thenReturn(bindingResult);
        when(bindingResult.getAllErrors())
                .thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, Object>> response =
                handler.handleValidation(ex);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody())
                .containsEntry("error", "Validation Failed");

        @SuppressWarnings("unchecked")
        Map<String, String> messages =
                (Map<String, String>) response
                        .getBody().get("messages");
        assertThat(messages)
                .containsEntry("email", "Invalid email");
    }

    @Test
    @DisplayName("Handle general Exception - 500")
    void handleGeneral_Returns500() {
        Exception ex = new Exception(
                "Unexpected error");

        ResponseEntity<Map<String, Object>> response =
                handler.handleGeneral(ex);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody())
                .containsEntry("status", 500);
    }

    @Test
    @DisplayName("ResourceNotFoundException has correct message")
    void resourceNotFoundException_HasCorrectMessage() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException(
                        "User not found with id: 99");

        assertThat(ex.getMessage())
                .isEqualTo("User not found with id: 99");
    }
}