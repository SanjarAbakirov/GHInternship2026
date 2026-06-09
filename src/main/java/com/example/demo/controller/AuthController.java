package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.User;
import com.example.demo.securities.JwtUtil;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        User newUser = userService.registerUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );
        String token = jwtUtil.generateToken(newUser.getUsername());
        AuthResponse response = new AuthResponse(
                true,
                "User registered successfully!",
                newUser.getUsername(),
                token
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody LoginRequest request) {
        User user = userService.authenticateUser(
                request.getUsername(),
                request.getPassword()
        );
        String token = jwtUtil.generateToken(user.getUsername());
        AuthResponse response = new AuthResponse(
                true,
                "Login successful!",
                user.getUsername(),
                token
        );
        return ResponseEntity.ok(response);
    }
}