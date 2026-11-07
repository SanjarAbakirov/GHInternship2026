package com.ghInternship.GHInternship2026.service;

import com.ghInternship.GHInternship2026.dto.ChatRequestDto;
import com.ghInternship.GHInternship2026.dto.ChatResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Модульные тесты для {@link ChatServiceImpl}.
 * Мокируют HTTP-клиент {@link RestTemplate} через Mockito, гарантируя скорость и независимость
 * от реального интернет-соединения и внешних AI API ключей.
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    private static final String TEST_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String TEST_API_KEY = "test-sk-123456789abcdef";

    @Mock
    private RestTemplate restTemplate;

    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatServiceImpl(restTemplate, TEST_API_URL, TEST_API_KEY);
    }

    @Test
    @DisplayName("Should correctly construct HTTP request with headers/body and return parsed AI response on HTTP 200")
    @SuppressWarnings("unchecked")
    void shouldConstructRequestCorrectlyAndReturnAiResponseOn200() {
        String mockOpenAiJsonResponse = """
                {
                    "id": "chatcmpl-123",
                    "object": "chat.completion",
                    "choices": [
                        {
                            "index": 0,
                            "message": {
                                "role": "assistant",
                                "content": "Spring Boot is an open-source Java framework."
                            },
                            "finish_reason": "stop"
                        }
                    ]
                }
                """;

        when(restTemplate.postForEntity(eq(TEST_API_URL), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(mockOpenAiJsonResponse));

        ChatRequestDto requestDto = new ChatRequestDto("What is Spring Boot?");
        ChatResponseDto responseDto = chatService.processMessage(requestDto);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.response()).isEqualTo("Spring Boot is an open-source Java framework.");
        assertThat(responseDto.timestamp()).isNotNull();

        // Проверяем корректность конструирования HTTP-запроса через ArgumentCaptor
        ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq(TEST_API_URL), entityCaptor.capture(), eq(String.class));

        HttpEntity<Map<String, Object>> capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity).isNotNull();

        // Проверка HTTP-заголовков
        HttpHeaders capturedHeaders = capturedEntity.getHeaders();
        assertThat(capturedHeaders.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(capturedHeaders.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer " + TEST_API_KEY);

        // Проверка тела HTTP-запроса
        Map<String, Object> capturedBody = capturedEntity.getBody();
        assertThat(capturedBody).isNotNull();
        assertThat(capturedBody.get("model")).isEqualTo("gpt-3.5-turbo");

        List<Map<String, String>> messages = (List<Map<String, String>>) capturedBody.get("messages");
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0))
                .containsEntry("role", "user")
                .containsEntry("content", "What is Spring Boot?");
    }

    @Test
    @DisplayName("Should handle plain text or custom JSON response body gracefully")
    void shouldHandlePlainTextResponseGracefully() {
        when(restTemplate.postForEntity(eq(TEST_API_URL), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("Simple plain text answer from AI"));

        ChatResponseDto responseDto = chatService.processMessage(new ChatRequestDto("Ping"));

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.response()).isEqualTo("Simple plain text answer from AI");
    }

    @Test
    @DisplayName("Should handle HttpClientErrorException (401 Unauthorized) when API key is invalid")
    void shouldHandleHttpClientErrorExceptionWhen401() {
        when(restTemplate.postForEntity(eq(TEST_API_URL), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Invalid API key"));

        assertThatThrownBy(() -> chatService.processMessage(new ChatRequestDto("Hello")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Error communicating with external AI service")
                .hasMessageContaining("401");
    }

    @Test
    @DisplayName("Should handle HttpServerErrorException (500 Internal Server Error) when AI API server fails")
    void shouldHandleHttpServerErrorExceptionWhen500() {
        when(restTemplate.postForEntity(eq(TEST_API_URL), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Service Overloaded"));

        assertThatThrownBy(() -> chatService.processMessage(new ChatRequestDto("Hello")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Error communicating with external AI service")
                .hasMessageContaining("500");
    }

    @Test
    @DisplayName("Should handle ResourceAccessException when network connection fails or times out")
    void shouldHandleResourceAccessExceptionOnNetworkFailure() {
        when(restTemplate.postForEntity(eq(TEST_API_URL), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection timed out"));

        assertThatThrownBy(() -> chatService.processMessage(new ChatRequestDto("Hello")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to communicate with external AI service")
                .hasMessageContaining("Connection timed out");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when requestDto is null or blank without calling HTTP client")
    void shouldThrowExceptionWhenRequestDtoIsInvalid() {
        assertThatThrownBy(() -> chatService.processMessage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");

        assertThatThrownBy(() -> chatService.processMessage(new ChatRequestDto("   ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");

        verifyNoInteractions(restTemplate);
    }
}
