package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "encodedPassword");
        testUser.setId(1L);
    }

    @Test
    void registerUser_Success() {
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");
        when(userService.registerUser(anyString(), anyString(), anyString())).thenReturn(testUser);

        ResponseEntity<?> response = authController.registerUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void registerUser_UsernameExists() {
        RegisterRequest request = new RegisterRequest("existinguser", "test@example.com", "password123");
        when(userService.registerUser(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Username already exists!"));

        ResponseEntity<?> response = authController.registerUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void loginUser_Success() {
        LoginRequest request = new LoginRequest("testuser", "password123");
        when(userService.authenticateUser("testuser", "password123")).thenReturn(testUser);

        ResponseEntity<?> response = authController.loginUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void loginUser_InvalidCredentials() {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        when(userService.authenticateUser("testuser", "wrongpassword"))
                .thenThrow(new RuntimeException("Invalid password!"));

        ResponseEntity<?> response = authController.loginUser(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}