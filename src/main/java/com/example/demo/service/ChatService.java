package com.example.demo.service;

import com.example.demo.dto.ChatMessageResponse;
import com.example.demo.dto.ChatResponse;
import com.example.demo.dto.ChatSessionResponse;
import com.example.demo.exception.AiServiceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.ChatMessage;
import com.example.demo.model.ChatSession;
import com.example.demo.model.User;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ChatSessionRepository;
import com.example.demo.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ChatService.class);
    private static final int TITLE_MAX_LENGTH = 80;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.model:deepseek-chat}")
    private String apiModel;

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    public ChatService(
            RestTemplate restTemplate,
            UserRepository userRepository,
            ChatSessionRepository chatSessionRepository,
            ChatMessageRepository chatMessageRepository) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
    }

    /**
     * Persists the conversation and returns the AI reply with the session id.
     * Creates a new {@link ChatSession} when {@code chatSessionId} is null.
     *
     * Intentionally NOT wrapped in a single {@code @Transactional}: the AI HTTP
     * call in the middle can take seconds, and holding a DB transaction (and its
     * pooled connection) open for that long would starve the connection pool.
     * Each repository call below is already transactional on its own via Spring
     * Data JPA.
     */
    public ChatResponse chat(String username, String userMessage, Long chatSessionId) {
        User user = requireUser(username);

        ChatSession session = resolveSession(user, chatSessionId, userMessage);

        ChatMessage userChatMessage = new ChatMessage(userMessage, ChatMessage.ROLE_USER, session);
        session.addMessage(userChatMessage);
        chatMessageRepository.save(userChatMessage);
        log.info("Saved user message for session {}", session.getId());

        String aiReply = getChatReply(userMessage);

        ChatMessage aiChatMessage = new ChatMessage(aiReply, ChatMessage.ROLE_AI, session);
        session.addMessage(aiChatMessage);
        chatMessageRepository.save(aiChatMessage);
        log.info("Saved AI message for session {}", session.getId());

        return new ChatResponse(aiReply, session.getId());
    }

    /**
     * Returns all chat sessions for the authenticated user, ordered by last activity.
     */
    @Transactional(readOnly = true)
    public List<ChatSessionResponse> listSessions(String username) {
        User user = requireUser(username);
        return chatSessionRepository.findByUserIdOrderByLastActivityDesc(user.getId()).stream()
                .map(session -> new ChatSessionResponse(
                        session.getId(),
                        session.getTitle(),
                        session.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Returns messages for a session owned by the authenticated user.
     * Throws {@link ResourceNotFoundException} if the session is missing or belongs to someone else.
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getSessionMessages(String username, Long sessionId) {
        User user = requireUser(username);
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Chat session not found or not owned by user: " + sessionId));

        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()).stream()
                .map(message -> new ChatMessageResponse(
                        message.getId(),
                        message.getContent(),
                        message.getRole(),
                        message.getCreatedAt()))
                .collect(Collectors.toList());
    }

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private ChatSession resolveSession(User user, Long chatSessionId, String firstMessage) {
        if (chatSessionId == null) {
            String title = buildTitleFromMessage(firstMessage);
            ChatSession created = chatSessionRepository.save(new ChatSession(title, user));
            log.info("Created chat session {} for user {}", created.getId(), user.getUsername());
            return created;
        }

        return chatSessionRepository.findByIdAndUserId(chatSessionId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Chat session not found or not owned by user: " + chatSessionId));
    }

    private String buildTitleFromMessage(String message) {
        String normalized = message == null ? "" : message.trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return "New chat";
        }
        if (normalized.length() <= TITLE_MAX_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, TITLE_MAX_LENGTH - 3) + "...";
    }

    public String getChatReply(String userMessage) {
        try {
            HttpHeaders headers = new HttpHeaders();
            if (apiKey != null && !apiKey.trim().isEmpty() && !apiKey.contains("placeholder")) {
                headers.setBearerAuth(apiKey.trim());
            }
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("HTTP-Referer", "http://localhost:3000");
            headers.set("X-Title", "GHInternship AI Chat");

            Map<String, Object> body = Map.of(
                    "model", apiModel,
                    "messages", List.of(Map.of("role", "user", "content", userMessage)),
                    "max_tokens", 150
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            log.warn("AI service call unavailable: {}. Returning fallback response.", e.getMessage());
            return "AI (Offline Mock): Внешний API провайдер недоступен или API-ключ OpenAI истек. Ваше сообщение сохранены в сессии: \"" + userMessage + "\"";
        }
    }
}
