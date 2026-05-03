package com.example.demo.controller;

import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


// REST контроллер для тестирования шифрования паролей
// @RestController - этот класс обрабатывает HTTP запросы

@RestController
@RequestMapping("/api/auth")  // Все методы будут доступны по адресу /api/auth/...
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
//     pass userService to constructor by using final

    /**
     * Тестируем шифрование пароля
     * Пример запроса: POST /api/auth/encode
     * Тело запроса (JSON): {"password": "mypassword123"}
     */
    @PostMapping("/encode")
    public Map<String, String> encodePassword(@RequestBody Map<String, String> request) {
        String rawPassword = request.get("password");

        // Шифруем пароль
        String encodedPassword = userService.encodePassword(rawPassword);

        // Отправляем ответ
        Map<String, String> response = new HashMap<>();
        response.put("originalPassword", rawPassword);
        response.put("encodedPassword", encodedPassword);
        response.put("message", "Пароль зашифрован! Сохрани encodedPassword в базу данных");

        return response;
    }

    /**
     * Тестируем проверку пароля
     * Пример запроса: POST /api/auth/verify
     * Тело запроса (JSON):
     * {
     *   "rawPassword": "mypassword123",
     *   "encodedPassword": "$2a$10$Nupz6b.8pJmX3IwEWOqFKe..."
     * }
     */
    @PostMapping("/verify")
    public Map<String, Object> verifyPassword(@RequestBody Map<String, String> request) {
        String rawPassword = request.get("rawPassword");
        String encodedPassword = request.get("encodedPassword");

        // Проверяем пароль
        boolean isValid = userService.verifyPassword(rawPassword, encodedPassword);

        // Отправляем ответ
        Map<String, Object> response = new HashMap<>();
        response.put("isValid", isValid);
        response.put("message", isValid ? "✅ Пароль верный!" : "❌ Пароль неверный!");

        return response;
    }
}