package com.example.demo.service;

import com.example.demo.dto.ChatResponse;
import com.example.demo.dto.ConversationDetailResponse;
import com.example.demo.dto.ConversationResponse;
import com.example.demo.dto.MessageResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Conversation;
import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.MessageRepository;
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
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ChatService(
            RestTemplate restTemplate,
            UserRepository userRepository,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * Persists the conversation and returns the AI reply with the conversation id.
     * Creates a new {@link Conversation} when {@code conversationId} is null.
     *
     * Intentionally NOT wrapped in a single {@code @Transactional}: the AI HTTP
     * call in the middle can take seconds, and holding a DB transaction (and its
     * pooled connection) open for that long would starve the connection pool.
     * Each repository call below is already transactional on its own via Spring
     * Data JPA.
     */
    public ChatResponse chat(String username, String userMessage, Long conversationId, String modelNameOverride) {
        User user = requireUser(username);
        boolean isNewConversation = conversationId == null;

        Conversation conversation = resolveConversation(user, conversationId, userMessage, modelNameOverride);

        Message userMsg = new Message(userMessage, Message.ROLE_USER, conversation);
        conversation.addMessage(userMsg);
        messageRepository.save(userMsg);
        log.info("Saved user message for conversation {}", conversation.getId());

        String aiReply = getChatReply(userMessage, conversation.getModelName());

        Message aiMsg = new Message(aiReply, Message.ROLE_AI, conversation);
        conversation.addMessage(aiMsg);
        messageRepository.save(aiMsg);
        log.info("Saved AI message for conversation {}", conversation.getId());

        // addMessage() above only bumps updatedAt in memory; persist it so
        // findByUserIdOrderByUpdatedAtDesc reflects this conversation's new activity.
        conversationRepository.save(conversation);

        return new ChatResponse(
                aiReply, conversation.getId(), conversation.getTitle(), isNewConversation, aiMsg.getCreatedAt());
    }

    /**
     * Returns all conversations for the authenticated user, ordered by last activity.
     */
    @Transactional(readOnly = true)
    public List<ConversationResponse> listSessions(String username) {
        User user = requireUser(username);
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(user.getId()).stream()
                .map(this::toConversationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Returns conversation metadata plus the full ordered message list, for a conversation
     * owned by the authenticated user.
     * Throws {@link ResourceNotFoundException} if the conversation is missing or belongs to someone else.
     */
    @Transactional(readOnly = true)
    public ConversationDetailResponse getConversationDetail(String username, Long conversationId) {
        User user = requireUser(username);
        Conversation conversation = conversationRepository.findByIdAndUserId(conversationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found or not owned by user: " + conversationId));

        List<MessageResponse> messages =
                messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId()).stream()
                        .map(message -> new MessageResponse(
                                message.getId(),
                                message.getContent(),
                                message.getRole(),
                                message.getCreatedAt()))
                        .collect(Collectors.toList());

        return new ConversationDetailResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getModelName(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                messages);
    }

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private Conversation resolveConversation(
            User user, Long conversationId, String firstMessage, String modelNameOverride) {
        if (conversationId == null) {
            String title = buildTitleFromMessage(firstMessage);
            String modelName = (modelNameOverride == null || modelNameOverride.isBlank())
                    ? apiModel
                    : modelNameOverride;
            Conversation created = conversationRepository.save(new Conversation(title, user, modelName));
            log.info("Created conversation {} for user {}", created.getId(), user.getUsername());
            return created;
        }

        return conversationRepository.findByIdAndUserId(conversationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conversation not found or not owned by user: " + conversationId));
    }

    /**
     * Builds the {@code messageCount}/{@code lastMessagePreview} fields with one extra query each
     * per conversation. Acceptable N+1 at this project's scale; batch with a grouped query if the
     * number of conversations per user grows significantly.
     */
    private ConversationResponse toConversationResponse(Conversation conversation) {
        long messageCount = messageRepository.countByConversationId(conversation.getId());
        String lastMessagePreview = messageRepository
                .findFirstByConversationIdOrderByCreatedAtDesc(conversation.getId())
                .map(Message::getContent)
                .map(this::truncate)
                .orElse(null);

        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getModelName(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                messageCount,
                lastMessagePreview);
    }

    private String buildTitleFromMessage(String message) {
        String normalized = message == null ? "" : message.trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return "New chat";
        }
        return truncate(normalized);
    }

    private String truncate(String text) {
        String normalized = text == null ? "" : text.trim().replaceAll("\\s+", " ");
        if (normalized.length() <= TITLE_MAX_LENGTH) {
            return normalized;
        }
        return normalized.substring(0, TITLE_MAX_LENGTH - 3) + "...";
    }

    public String getChatReply(String userMessage) {
        return getChatReply(userMessage, null);
    }

    /**
     * @param modelOverride model to use for this call; falls back to the configured
     *                      default ({@code openai.api.model}) when null/blank (e.g. for
     *                      conversations created before the model_name column existed).
     */
    public String getChatReply(String userMessage, String modelOverride) {
        String model = (modelOverride == null || modelOverride.isBlank()) ? apiModel : modelOverride;
        try {
            HttpHeaders headers = new HttpHeaders();
            if (apiKey != null && !apiKey.trim().isEmpty() && !apiKey.contains("placeholder")) {
                headers.setBearerAuth(apiKey.trim());
            }
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("HTTP-Referer", "http://localhost:3000");
            headers.set("X-Title", "GHInternship AI Chat");

            Map<String, Object> body = Map.of(
                    "model", model,
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
