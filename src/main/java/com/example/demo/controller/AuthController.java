//package com.example.demo.controller;
//
//import com.example.demo.service.UserService;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/auth")
//public class AuthController {
//    private final UserService userService;
//
//    // Явный конструктор - самый надежный способ
//    public AuthController(UserService userService) {
//        this.userService = userService;
//    }
//
//    @PostMapping("/encode")
//    public Map<String, String> encodePassword(@RequestBody Map<String, String> request) {
//        String rawPassword = request.get("password");
//        String encodedPassword = userService.encodePassword(rawPassword);
//
//        Map<String, String> response = new HashMap<>();
//        response.put("originalPassword", rawPassword);
//        response.put("encodedPassword", encodedPassword);
//        response.put("message", "Пароль зашифрован! Сохрани encodedPassword в базу данных");
//
//        return response;
//    }
//
//    @PostMapping("/verify")
//    public Map<String, Object> verifyPassword(@RequestBody Map<String, String> request) {
//        String rawPassword = request.get("rawPassword");
//        String encodedPassword = request.get("encodedPassword");
//        boolean isValid = userService.verifyPassword(rawPassword, encodedPassword);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("isValid", isValid);
//        response.put("message", isValid ? "✅ Пароль верный!" : "❌ Пароль неверный!");
//
//        return response;
//    }
//}