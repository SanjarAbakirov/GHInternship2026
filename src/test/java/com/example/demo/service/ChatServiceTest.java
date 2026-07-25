package com.example.demo.service;

import com.example.demo.exception.AiServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ChatServiceTest {

    private RestTemplate restTemplateMock;
    private ChatService chatService;

    @BeforeEach
    public void setUp() {
        restTemplateMock = Mockito.mock(RestTemplate.class);
        chatService = new ChatService(restTemplateMock);
        
        ReflectionTestUtils.setField(chatService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(chatService, "apiUrl", "http://test.api.url");
        ReflectionTestUtils.setField(chatService, "apiModel", "gpt-3.5-turbo");
    }

    @Test
    public void getChatReply_success() {
        String expectedReply = "Hello from AI";
        
        // Готовим структуру JSON, которую обычно возвращает OpenAI
        Map<String, Object> messageMap = Map.of("content", expectedReply);
        Map<String, Object> choiceMap = Map.of("message", messageMap);
        Map<String, Object> responseBody = Map.of("choices", List.of(choiceMap));
        
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        
        // Мокаем внешний вызов API (никакого реального интернета)
        Mockito.when(restTemplateMock.postForEntity(
                ArgumentMatchers.eq("http://test.api.url"),
                ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(Map.class)
        )).thenReturn(responseEntity);
        
        // Выполняем тест
        String actualReply = chatService.getChatReply("Hello");
        assertEquals(expectedReply, actualReply);
    }

    @Test
    public void getChatReply_error() {
        // Мокаем сетевую ошибку (например, таймаут или нет интернета)
        Mockito.when(restTemplateMock.postForEntity(
                ArgumentMatchers.eq("http://test.api.url"),
                ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(Map.class)
        )).thenThrow(new RestClientException("Connection refused"));
        
        // Убеждаемся, что сервис ловит ошибку и выбрасывает наше кастомное исключение
        AiServiceException exception = assertThrows(AiServiceException.class, () -> {
            chatService.getChatReply("Hello");
        });
        
        assertEquals("AI service is currently unavailable. Please try again later.", exception.getMessage());
    }
}
