package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;

class AuthControllerTest {

    private AuthController authController;
    private UserServiceStub userServiceStub;
    private User testUser;

    @BeforeEach
    void setUp() {
        userServiceStub = new UserServiceStub();
        authController = new AuthController();
        try {
            var field = AuthController.class.getDeclaredField("userService");
            field.setAccessible(true);
            field.set(authController, userServiceStub);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        testUser = new User("testuser", "test@example.com", "encodedPassword");
        testUser.setId(1L);
    }

    // ==================== POST /api/auth/register ====================

    @Test
    void registerUser_Success() {
        userServiceStub.setRegisterResult(testUser);
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");

        ResponseEntity<?> response = authController.registerUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        AuthResponse body = (AuthResponse) response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
    }

    @Test
    void registerUser_UsernameAlreadyExists() {
        userServiceStub.setRegisterException(new RuntimeException("Username already exists!"));
        RegisterRequest request = new RegisterRequest("existinguser", "test@example.com", "password123");

        ResponseEntity<?> response = authController.registerUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        userServiceStub.setRegisterException(new RuntimeException("Email already exists!"));
        RegisterRequest request = new RegisterRequest("newuser", "existing@example.com", "password123");

        ResponseEntity<?> response = authController.registerUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ==================== Validation Tests ====================

    @Test
    void registerUser_EmptyUsername_ReturnsBadRequest() {
        // Отправляем запрос с пустым username
        RegisterRequest request = new RegisterRequest("", "test@example.com", "password123");

        // Создаем ошибку валидации вручную
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "registerRequest");
        bindingResult.addError(new FieldError("registerRequest", "username", "Username is required"));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<?> response = authController.handleValidationExceptions(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AuthResponse body = (AuthResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertTrue(body.getMessage().contains("Username is required"));
    }

    @Test
    void registerUser_EmptyEmail_ReturnsBadRequest() {
        RegisterRequest request = new RegisterRequest("testuser", "", "password123");

        BindingResult bindingResult = new BeanPropertyBindingResult(request, "registerRequest");
        bindingResult.addError(new FieldError("registerRequest", "email", "Email is required"));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<?> response = authController.handleValidationExceptions(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AuthResponse body = (AuthResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertTrue(body.getMessage().contains("Email is required"));
    }

    @Test
    void registerUser_EmptyPassword_ReturnsBadRequest() {
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "");

        BindingResult bindingResult = new BeanPropertyBindingResult(request, "registerRequest");
        bindingResult.addError(new FieldError("registerRequest", "password", "Password is required"));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<?> response = authController.handleValidationExceptions(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AuthResponse body = (AuthResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertTrue(body.getMessage().contains("Password is required"));
    }

    @Test
    void registerUser_ShortPassword_ReturnsBadRequest() {
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "12345");

        BindingResult bindingResult = new BeanPropertyBindingResult(request, "registerRequest");
        bindingResult.addError(new FieldError("registerRequest", "password", "Password must be at least 6 characters"));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<?> response = authController.handleValidationExceptions(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AuthResponse body = (AuthResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertTrue(body.getMessage().contains("at least 6 characters"));
    }

    @Test
    void registerUser_InvalidEmail_ReturnsBadRequest() {
        RegisterRequest request = new RegisterRequest("testuser", "invalid-email", "password123");

        BindingResult bindingResult = new BeanPropertyBindingResult(request, "registerRequest");
        bindingResult.addError(new FieldError("registerRequest", "email", "Email should be valid"));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<?> response = authController.handleValidationExceptions(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AuthResponse body = (AuthResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertTrue(body.getMessage().contains("valid"));
    }

    // ==================== POST /api/auth/login ====================

    @Test
    void loginUser_Success() {
        userServiceStub.setAuthResult(testUser);
        LoginRequest request = new LoginRequest("testuser", "password123");

        ResponseEntity<?> response = authController.loginUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void loginUser_InvalidPassword() {
        userServiceStub.setAuthException(new RuntimeException("Invalid password!"));
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");

        ResponseEntity<?> response = authController.loginUser(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void loginUser_UserNotFound() {
        userServiceStub.setAuthException(new RuntimeException("User not found!"));
        LoginRequest request = new LoginRequest("nonexistent", "password123");

        ResponseEntity<?> response = authController.loginUser(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ==================== Stub ====================

    static class UserServiceStub extends UserService {
        private User registerResult;
        private RuntimeException registerException;
        private User authResult;
        private RuntimeException authException;

        public void setRegisterResult(User result) { this.registerResult = result; }
        public void setRegisterException(RuntimeException ex) { this.registerException = ex; }
        public void setAuthResult(User result) { this.authResult = result; }
        public void setAuthException(RuntimeException ex) { this.authException = ex; }

        @Override
        public User registerUser(String username, String email, String password) {
            if (registerException != null) throw registerException;
            return registerResult;
        }

        @Override
        public User authenticateUser(String username, String password) {
            if (authException != null) throw authException;
            return authResult;
        }
    }
}