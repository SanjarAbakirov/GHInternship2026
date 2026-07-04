package com.example.demo.exception;
import com.example.demo.dto.AuthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Ошибка AI-сервиса
    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<AuthResponse> handleAiServiceException(AiServiceException ex) {
        AuthResponse response = new AuthResponse(false, ex.getMessage(), null, null);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    // Ошибки дублирования пользователя (если используются)
    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<AuthResponse> handleDuplicateUser(DuplicateUserException ex) {
        AuthResponse response = new AuthResponse(false, ex.getMessage(), null, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Ошибки аутентификации (если используются)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<AuthResponse> handleAuthentication(AuthenticationException ex) {
        AuthResponse response = new AuthResponse(false, ex.getMessage(), null, null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // Ошибки валидации Bean Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        AuthResponse response = new AuthResponse(
                false,
                "Validation failed: " + errors.toString(),
                null,
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Общая ошибка 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponse> handleGeneral(Exception ex, WebRequest request) {
        log.error("Unhandled exception caught during request processing: ", ex);
        AuthResponse response = new AuthResponse(false, "Internal server error", null, null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
