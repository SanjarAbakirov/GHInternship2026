package com.example.demo.service;

import com.example.demo.dto.ChatResponse;
import com.example.demo.dto.ConversationDetailResponse;
import com.example.demo.dto.ConversationResponse;
import com.example.demo.dto.MessageResponse;
import com.example.demo.exception.ConversationNotFoundException;
import com.example.demo.exception.UnauthorizedConversationAccessException;
import com.example.demo.exception.UserNotFoundException;
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

    /** Caps how much prior conversation is sent to the AI as context per request. */
    private static final int MAX_HISTORY_MESSAGES = 20;

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

        // Part A: resolve the user and conversation (create new, or load + validate ownership).
        Conversation conversation = resolveConversation(user, conversationId, userMessage, modelNameOverride);

        // Part B: persist the user's message.
        Message userMsg = new Message(userMessage, Message.ROLE_USER, conversation);
        conversation.addMessage(userMsg);
        messageRepository.save(userMsg);
        log.info("Saved user message for conversation {}", conversation.getId());

        // Part C: reload the conversation history (including the message just saved) so the AI
        // has multi-turn context instead of only seeing the latest message in isolation.
        List<Message> history =
                capHistory(messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId()));
        String aiReply = getChatReply(history, conversation.getModelName());

        // Part D: persist the AI's reply.
        Message aiMsg = new Message(aiReply, Message.ROLE_AI, conversation);
        conversation.addMessage(aiMsg);
        messageRepository.save(aiMsg);
        log.info("Saved AI message for conversation {}", conversation.getId());

        // Part E: addMessage() above only bumps updatedAt in memory; persist it so
        // findByUserIdOrderByUpdatedAtDesc reflects this conversation's new activity, then
        // build the response DTO with full conversation context.
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
     * Throws {@link ConversationNotFoundException} if no such conversation exists, or
     * {@link UnauthorizedConversationAccessException} if it belongs to someone else.
     */
    @Transactional(readOnly = true)
    public ConversationDetailResponse getConversationDetail(String username, Long conversationId) {
        User user = requireUser(username);
        Conversation conversation = requireOwnedConversation(conversationId, user);

        List<MessageResponse> messages =
                messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId()).stream()
                        .map(this::toMessageResponse)
                        .collect(Collectors.toList());

        return new ConversationDetailResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getModelName(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                messages);
    }

    /**
     * Deletes a conversation (and, via cascade + orphanRemoval on {@link Conversation#getMessages()},
     * all of its messages) owned by the authenticated user.
     * Throws {@link ConversationNotFoundException} if no such conversation exists, or
     * {@link UnauthorizedConversationAccessException} if it belongs to someone else.
     */
    public void deleteConversation(String username, Long conversationId) {
        User user = requireUser(username);
        Conversation conversation = requireOwnedConversation(conversationId, user);

        conversationRepository.delete(conversation);
        log.info("Deleted conversation {} for user {}", conversationId, user.getUsername());
    }

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
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

        return requireOwnedConversation(conversationId, user);
    }

    /**
     * Loads a conversation by id and verifies it belongs to {@code user}, distinguishing "doesn't
     * exist" from "exists but isn't yours" so callers can return the right HTTP status (404 vs 403).
     */
    private Conversation requireOwnedConversation(Long conversationId, User user) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ConversationNotFoundException(
                        "Conversation not found: " + conversationId));

        if (!conversation.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedConversationAccessException(
                    "User " + user.getUsername() + " is not authorized to access conversation " + conversationId);
        }

        return conversation;
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

    private MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(message.getId(), message.getContent(), message.getRole(), message.getCreatedAt());
    }

    /** Keeps only the most recent messages so the AI request payload doesn't grow unbounded. */
    private List<Message> capHistory(List<Message> history) {
        if (history.size() <= MAX_HISTORY_MESSAGES) {
            return history;
        }
        return history.subList(history.size() - MAX_HISTORY_MESSAGES, history.size());
    }

    /** Our stored role ({@link Message#ROLE_AI}) differs from the OpenAI-style API role name. */
    private String toApiRole(String storedRole) {
        return Message.ROLE_AI.equals(storedRole) ? "assistant" : "user";
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
        return getChatReply(List.of(new Message(userMessage, Message.ROLE_USER)), modelOverride);
    }

    /**
     * Calls the AI service with the full ordered conversation history as context (multi-turn),
     * rather than just the latest message in isolation.
     *
     * @param history ordered oldest-first messages to send, expected to end with the latest user message.
     * @param modelOverride model to use for this call; falls back to the configured
     *                      default ({@code openai.api.model}) when null/blank.
     */
    private String getChatReply(List<Message> history, String modelOverride) {
        String model = (modelOverride == null || modelOverride.isBlank()) ? apiModel : modelOverride;
        String latestMessage = history.isEmpty() ? "" : history.get(history.size() - 1).getContent();
        try {
            HttpHeaders headers = new HttpHeaders();
            if (apiKey != null && !apiKey.trim().isEmpty() && !apiKey.contains("placeholder")) {
                headers.setBearerAuth(apiKey.trim());
            }
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("HTTP-Referer", "http://localhost:3000");
            headers.set("X-Title", "GHInternship AI Chat");

            List<Map<String, Object>> apiMessages = history.stream()
                    .map(m -> Map.<String, Object>of("role", toApiRole(m.getRole()), "content", m.getContent()))
                    .collect(Collectors.toList());

            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", apiMessages,
                    "max_tokens", 150
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            log.warn("AI service call unavailable: {}. Returning fallback response.", e.getMessage());
            return "AI (Offline Mock): Внешний API провайдер недоступен или API-ключ OpenAI истек. Ваше сообщение сохранены в сессии: \"" + latestMessage + "\"";
        }
    }
}
