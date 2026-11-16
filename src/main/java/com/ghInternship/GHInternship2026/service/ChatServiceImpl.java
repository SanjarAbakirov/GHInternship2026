package com.ghInternship.GHInternship2026.service;

import com.ghInternship.GHInternship2026.dto.ChatRequestDto;
import com.ghInternship.GHInternship2026.dto.ChatResponseDto;
import com.ghInternship.GHInternship2026.entity.ChatMessage;
import com.ghInternship.GHInternship2026.entity.ChatSession;
import com.ghInternship.GHInternship2026.entity.User;
import com.ghInternship.GHInternship2026.exception.ChatSessionNotFoundException;
import com.ghInternship.GHInternship2026.repository.ChatMessageRepository;
import com.ghInternship.GHInternship2026.repository.ChatSessionRepository;
import com.ghInternship.GHInternship2026.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Реализация сервиса {@link ChatService} с сохранением сессий диалогов и сообщений.
 * Обеспечивает персистентность переписки в PostgreSQL через {@link ChatSessionRepository}
 * и {@link ChatMessageRepository}, соблюдая изоляцию данных пользователей и выполняя
 * вызовы к внешнему AI-сервису.
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
    private final UserRepository userRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final String apiUrl;
    private final String apiKey;

    @Autowired
    public ChatServiceImpl(
            RestTemplate restTemplate,
            UserRepository userRepository,
            ChatSessionRepository chatSessionRepository,
            ChatMessageRepository chatMessageRepository,
            @Value("${ai.service.api-url:https://api.openai.com/v1/chat/completions}") String apiUrl,
            @Value("${ai.service.api-key:}") String apiKey
    ) {
        this.restTemplate = Objects.requireNonNull(restTemplate, "restTemplate must not be null");
        this.userRepository = userRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.apiUrl = (apiUrl != null && !apiUrl.isBlank()) ? apiUrl : "https://api.openai.com/v1/chat/completions";
        this.apiKey = (apiKey != null) ? apiKey.trim() : "";
    }

    /**
     * Конструктор для обратной совместимости в легковесных тестах без репозиториев.
     */
    public ChatServiceImpl(
            RestTemplate restTemplate,
            String apiUrl,
            String apiKey
    ) {
        this(restTemplate, null, null, null, apiUrl, apiKey);
    }

    @Override
    @Transactional
    public ChatResponseDto processMessage(ChatRequestDto requestDto, String username) {
        if (requestDto == null || requestDto.message() == null || requestDto.message().isBlank()) {
            log.warn("Попытка обработки пустого сообщения в ChatService");
            throw new IllegalArgumentException("Chat message must not be blank");
        }

        String userMessage = requestDto.message().trim();
        log.info("Обработка сообщения пользователя в ChatService (длина: {} символов)", userMessage.length());

        // 1. Определение текущего пользователя
        User user = resolveUser(username);

        // 2. Получение или создание сессии диалога
        ChatSession session = resolveOrCreateSession(requestDto.chatSessionId(), user, userMessage);

        // 3. Сохранение сообщения пользователя, связанного с текущей сессией
        if (session != null && chatMessageRepository != null) {
            ChatMessage userMsg = ChatMessage.userMessage(session, userMessage);
            session.addMessage(userMsg);
            chatMessageRepository.save(userMsg);
            log.info("Сохранено сообщение пользователя в сессию [id={}]", session.getId());
        }

        // 4. Вызов внешнего AI API
        String aiResponseText = callAiProvider(userMessage);

        // 5. Сохранение ответа AI, связанного с этой же сессией
        if (session != null && chatMessageRepository != null) {
            ChatMessage aiMsg = ChatMessage.aiMessage(session, aiResponseText);
            session.addMessage(aiMsg);
            chatMessageRepository.save(aiMsg);
            if (chatSessionRepository != null) {
                chatSessionRepository.save(session);
            }
            log.info("Сохранен ответ AI в сессию [id={}]", session.getId());
        }

        Long responseSessionId = (session != null) ? session.getId() : requestDto.chatSessionId();
        return new ChatResponseDto(responseSessionId, aiResponseText, LocalDateTime.now());
    }

    private User resolveUser(String username) {
        String effectiveUsername = username;
        if (effectiveUsername == null || effectiveUsername.isBlank()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                effectiveUsername = auth.getName();
            }
        }

        if (effectiveUsername != null && userRepository != null) {
            return userRepository.findByUsername(effectiveUsername).orElse(null);
        }
        return null;
    }

    private ChatSession resolveOrCreateSession(Long requestedSessionId, User user, String firstMessage) {
        if (chatSessionRepository == null) {
            return null;
        }

        if (requestedSessionId != null) {
            // Обращение к существующей сессии с проверкой прав доступа
            if (user != null) {
                return chatSessionRepository.findByIdAndUser(requestedSessionId, user)
                        .orElseThrow(() -> new ChatSessionNotFoundException(requestedSessionId, user.getUsername()));
            } else {
                return chatSessionRepository.findById(requestedSessionId)
                        .orElseThrow(() -> new ChatSessionNotFoundException(requestedSessionId));
            }
        }

        // Создание новой сессии диалога для пользователя
        if (user != null) {
            String title = ChatSession.generateTitleFromMessage(firstMessage);
            ChatSession newSession = new ChatSession(user, title);
            ChatSession savedSession = chatSessionRepository.save(newSession);
            log.info("Создана новая сессия диалога [id={}, title='{}'] для пользователя: {}",
                    savedSession.getId(), savedSession.getTitle(), user.getUsername());
            return savedSession;
        }

        return null;
    }

    private String callAiProvider(String userMessage) {
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
                return extractedContent;
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

        // 2. Фоллбэк: ищем поле "response"
        Matcher responseMatcher = RESPONSE_PATTERN.matcher(trimmed);
        if (responseMatcher.find()) {
            return unescapeJson(responseMatcher.group(1));
        }

        // 3. Если ответ не является обернутым JSON, возвращаем как есть
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return trimmed;
        }

        return trimmed;
    }

    private String unescapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
