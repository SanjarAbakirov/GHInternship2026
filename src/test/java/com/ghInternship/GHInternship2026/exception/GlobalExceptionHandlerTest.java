package com.ghInternship.GHInternship2026.exception;

import com.ghInternship.GHInternship2026.dto.ErrorResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Модульные тесты для {@link GlobalExceptionHandler}.
 * Проверяют корректность маппинга исключений в стандартизированные ответы {@link ErrorResponseDto}.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    @DisplayName("handleUserAlreadyExists should return 409 Conflict with ErrorResponseDto")
    void handleUserAlreadyExists_ShouldReturn409() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("User 'alice' already exists");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleUserAlreadyExists(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().error()).isEqualTo("Conflict");
        assertThat(response.getBody().message()).isEqualTo("User 'alice' already exists");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleInvalidCredentials should return 401 Unauthorized with ErrorResponseDto")
    void handleInvalidCredentials_ShouldReturn401() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid username or password");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleInvalidCredentials(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(401);
        assertThat(response.getBody().error()).isEqualTo("Unauthorized");
        assertThat(response.getBody().message()).isEqualTo("Invalid username or password");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("handleUserNotFound should return 404 Not Found with ErrorResponseDto")
    void handleUserNotFound_ShouldReturn404() {
        UserNotFoundException ex = new UserNotFoundException("User not found with id: 42");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleUserNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().message()).isEqualTo("User not found with id: 42");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }

    @Test
    @DisplayName("handleValidationException should return 400 Bad Request with field errors map")
    void handleValidationException_ShouldReturn400_WithFieldErrors() {
        Object target = new Object();
        BindingResult bindingResult = new BeanPropertyBindingResult(target, "userRegistrationDto");
        bindingResult.addError(new FieldError("userRegistrationDto", "username", "Username is required"));
        bindingResult.addError(new FieldError("userRegistrationDto", "password", "Password is too short"));

        ValidationException ex = new ValidationException(bindingResult);

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleValidationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().validationErrors()).isNotNull();
        assertThat(response.getBody().validationErrors()).containsEntry("username", "Username is required");
        assertThat(response.getBody().validationErrors()).containsEntry("password", "Password is too short");
    }

    @Test
    @DisplayName("handleHttpMessageNotReadable should return 400 Bad Request")
    void handleHttpMessageNotReadable_ShouldReturn400() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "Required request body is missing",
                new MockHttpInputMessage(new byte[0])
        );

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleHttpMessageNotReadable(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).contains("Required request body is missing or invalid");
    }

    @Test
    @DisplayName("handleIllegalArgument should return 400 Bad Request")
    void handleIllegalArgument_ShouldReturn400() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument provided");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleIllegalArgument(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Bad Request");
        assertThat(response.getBody().message()).isEqualTo("Invalid argument provided");
    }

    @Test
    @DisplayName("handleAllUncaughtExceptions should return 500 Internal Server Error without leaking internal details")
    void handleAllUncaughtExceptions_ShouldReturn500() {
        RuntimeException ex = new RuntimeException("Unexpected database timeout");

        ResponseEntity<ErrorResponseDto> response = exceptionHandler.handleAllUncaughtExceptions(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().message()).isEqualTo("An unexpected internal server error occurred");
        assertThat(response.getBody().path()).isEqualTo("/api/test");
    }
}
