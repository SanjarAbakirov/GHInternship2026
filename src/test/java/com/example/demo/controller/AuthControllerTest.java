package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Standalone режим - без загрузки Spring контекста
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();

        testUser = new User("testuser", "test@example.com", "encodedPassword");
        testUser.setId(1L);
    }

    // ==================== POST /api/auth/register ====================

    @Test
    void registerUser_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("testuser", "test@example.com", "password123");
        when(userService.registerUser("testuser", "test@example.com", "password123"))
                .thenReturn(testUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully!"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void registerUser_UsernameAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest("existinguser", "test@example.com", "password123");
        when(userService.registerUser("existinguser", "test@example.com", "password123"))
                .thenThrow(new RuntimeException("Username already exists!"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username already exists!"));
    }

    @Test
    void registerUser_EmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest("newuser", "existing@example.com", "password123");
        when(userService.registerUser("newuser", "existing@example.com", "password123"))
                .thenThrow(new RuntimeException("Email already exists!"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already exists!"));
    }

    // ==================== POST /api/auth/login ====================

    @Test
    void loginUser_Success() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "password123");
        when(userService.authenticateUser("testuser", "password123"))
                .thenReturn(testUser);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful!"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void loginUser_InvalidPassword() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        when(userService.authenticateUser("testuser", "wrongpassword"))
                .thenThrow(new RuntimeException("Invalid password!"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid password!"));
    }

    @Test
    void loginUser_UserNotFound() throws Exception {
        LoginRequest request = new LoginRequest("nonexistent", "password123");
        when(userService.authenticateUser("nonexistent", "password123"))
                .thenThrow(new RuntimeException("User not found!"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found!"));
    }
}