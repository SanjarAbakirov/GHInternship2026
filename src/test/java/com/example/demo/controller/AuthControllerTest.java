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

import static org.junit.jupiter.api.Assertions.*;

class AuthControllerTest {

    private AuthController authController;
    private UserServiceStub userServiceStub;
    private User testUser;

    @BeforeEach
    void setUp() {
        userServiceStub = new UserServiceStub();
        authController = new AuthController();
        // Внедряем заглушку через reflection
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
        assertEquals("User registered successfully!", body.getMessage());
        assertEquals("testuser", body.getUsername());
    }

    @Test
    void registerUser_UsernameAlreadyExists() {
        userServiceStub.setRegisterException(new RuntimeException("Username already exists!"));
        RegisterRequest request = new RegisterRequest("existinguser", "test@example.com", "password123");

        ResponseEntity<?> response = authController.registerUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AuthResponse body = (AuthResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Username already exists!", body.getMessage());
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        userServiceStub.setRegisterException(new RuntimeException("Email already exists!"));
        RegisterRequest request = new RegisterRequest("newuser", "existing@example.com", "password123");

        ResponseEntity<?> response = authController.registerUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AuthResponse body = (AuthResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Email already exists!", body.getMessage());
    }

    // ==================== POST /api/auth/login ====================

    @Test
    void loginUser_Success() {
        userServiceStub.setAuthResult(testUser);
        LoginRequest request = new LoginRequest("testuser", "password123");

        ResponseEntity<?> response = authController.loginUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponse body = (AuthResponse) response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());
        assertEquals("Login successful!", body.getMessage());
        assertEquals("testuser", body.getUsername());
    }

    @Test
    void loginUser_InvalidPassword() {
        userServiceStub.setAuthException(new RuntimeException("Invalid password!"));
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");

        ResponseEntity<?> response = authController.loginUser(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        AuthResponse body = (AuthResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("Invalid password!", body.getMessage());
    }

    @Test
    void loginUser_UserNotFound() {
        userServiceStub.setAuthException(new RuntimeException("User not found!"));
        LoginRequest request = new LoginRequest("nonexistent", "password123");

        ResponseEntity<?> response = authController.loginUser(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        AuthResponse body = (AuthResponse) response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertEquals("User not found!", body.getMessage());
    }

    // ==================== Stub (заглушка вместо Mockito) ====================

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