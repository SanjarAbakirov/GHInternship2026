package com.ghInternship.GHInternship2026.service;

import com.ghInternship.GHInternship2026.dto.ChatRequestDto;
import com.ghInternship.GHInternship2026.dto.ChatResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Реализация интерфейса {@link ChatService}.
 * Содержит логику конструирования HTTP-запроса и выполнения вызова к внешнему AI API
 * через HTTP-клиент {@link RestTemplate}.
 */
@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private static final Pattern CONTENT_PATTERN = Pattern.compile(
            "\"content\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
    );
    private static final Pattern RESPONSE_PATTERN = Pattern.compile(
            "\"response\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
    );

    private final RestTemplate restTemplate;
    private final String apiUrl;
    private final String apiKey;

    public ChatServiceImpl(
            RestTemplate restTemplate,
            @Value("${ai.service.api-url:https://api.openai.com/v1/chat/completions}") String apiUrl,
            @Value("${ai.service.api-key:}") String apiKey
    ) {
        this.restTemplate = Objects.requireNonNull(restTemplate, "restTemplate must not be null");
        this.apiUrl = (apiUrl != null && !apiUrl.isBlank()) ? apiUrl : "https://api.openai.com/v1/chat/completions";
        this.apiKey = (apiKey != null) ? apiKey.trim() : "";
    }

    @Override
    public ChatResponseDto processMessage(ChatRequestDto requestDto) {
        if (requestDto == null || requestDto.message() == null || requestDto.message().isBlank()) {
            log.warn("Попытка обработки пустого сообщения в ChatService");
            throw new IllegalArgumentException("Chat message must not be blank");
        }

        String userMessage = requestDto.message().trim();
        log.info("Обработка сообщения пользователя в ChatService (длина: {} символов)", userMessage.length());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (!apiKey.isBlank()) {
            headers.setBearerAuth(apiKey);
        }

        Map<String, Object> requestPayload = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                        Map.of("role", "user", "content", userMessage)
                )
        );

        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestPayload, headers);

        try {
            log.debug("Отправка HTTP POST запроса к внешнему AI API: {}", apiUrl);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, httpEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String extractedContent = extractContentFromResponseBody(response.getBody());
                log.info("Успешно получен ответ от внешнего AI API");
                return new ChatResponseDto(extractedContent);
            } else {
                log.error("Внешний AI API вернул неожиданный статус: {}", response.getStatusCode());
                throw new IllegalStateException("External AI API returned unexpected status: " + response.getStatusCode());
            }
        } catch (HttpStatusCodeException ex) {
            log.error("Ошибка HTTP при обращении к внешнему AI API: статус={}, тело={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new IllegalStateException("Error communicating with external AI service: " + ex.getStatusCode(), ex);
        } catch (RestClientException ex) {
            log.error("Сетевая ошибка при обращении к внешнему AI API: {}", ex.getMessage());
            throw new IllegalStateException("Failed to communicate with external AI service: " + ex.getMessage(), ex);
        }
    }

    private String extractContentFromResponseBody(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "";
        }
        String trimmed = responseBody.trim();

        // 1. Извлекаем поле "content" из структуры ответа OpenAI/совместимого формата
        Matcher contentMatcher = CONTENT_PATTERN.matcher(trimmed);
        if (contentMatcher.find()) {
            return unescapeJson(contentMatcher.group(1));
        }

        // 2. Извлекаем поле "response", если внешний API использует упрощенный JSON формат
        Matcher responseMatcher = RESPONSE_PATTERN.matcher(trimmed);
        if (responseMatcher.find()) {
            return unescapeJson(responseMatcher.group(1));
        }

        // 3. Если ответ обернут в JSON-кавычки
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            return unescapeJson(trimmed.substring(1, trimmed.length() - 1));
        }

        // 4. Fallback: возвращаем исходный текст ответа
        return trimmed;
    }

    private String unescapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getApiKey() {
        return apiKey;
    }
}
