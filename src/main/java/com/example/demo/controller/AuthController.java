package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // Эндпоинт для регистрации
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            // Регистрируем пользователя
            User newUser = userService.registerUser(
                    registerRequest.getUsername(),
                    registerRequest.getEmail(),
                    registerRequest.getPassword()
            );

            // Успешный ответ с кодом 201 Created
            AuthResponse response = new AuthResponse(
                    true,
                    "User registered successfully!",
                    newUser.getUsername()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            // Ошибка при регистрации
            AuthResponse response = new AuthResponse(
                    false,
                    e.getMessage(),
                    null
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // Эндпоинт для логина
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            // Аутентифицируем пользователя
            User authenticatedUser = userService.authenticateUser(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );

            // Успешный ответ
            AuthResponse response = new AuthResponse(
                    true,
                    "Login successful!",
                    authenticatedUser.getUsername()
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Ошибка аутентификации
            AuthResponse response = new AuthResponse(
                    false,
                    e.getMessage(),
                    null
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}