package com.example.demo.controller;

import com.example.demo.config.SecurityConfig;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

// Запускаем тест только для ChatController
@WebMvcTest(ChatController.class)
// Подтягиваем нашу реальную конфигурацию безопасности, чтобы проверить 401 статус
@Import({SecurityConfig.java, JwtAuthenticationFilter.java, JwtUtil.java})
// Указываем тестовый секретный ключ, чтобы JwtUtil смог запуститься
@TestPropertySource(properties = "jwt.secret=my-super-secret-key-for-tests-1234567890")
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Изолируем реальный ChatService (мы не хотим делать реальные запросы к OpenAI в тестах)
    @MockitoBean
    private ChatService chatService;

}