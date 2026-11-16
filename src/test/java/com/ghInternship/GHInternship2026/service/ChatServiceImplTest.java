package com.ghInternship.GHInternship2026.service;

import com.ghInternship.GHInternship2026.dto.ChatMessageResponseDto;
import com.ghInternship.GHInternship2026.dto.ChatRequestDto;
import com.ghInternship.GHInternship2026.dto.ChatResponseDto;
import com.ghInternship.GHInternship2026.dto.ChatSessionResponseDto;
import com.ghInternship.GHInternship2026.entity.ChatMessage;
import com.ghInternship.GHInternship2026.entity.ChatSession;
import com.ghInternship.GHInternship2026.entity.User;
import com.ghInternship.GHInternship2026.exception.ChatSessionNotFoundException;
import com.ghInternship.GHInternship2026.exception.UserNotFoundException;
import com.ghInternship.GHInternship2026.repository.ChatMessageRepository;
import com.ghInternship.GHInternship2026.repository.ChatSessionRepository;
import com.ghInternship.GHInternship2026.repository.UserRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Модульные тесты для {@link ChatServiceImpl}.
 * Мокируют HTTP-клиент {@link RestTemplate} и JPA-репозитории через Mockito,
 * проверяя конструирование запросов к AI и персистентность сессий/сообщений.
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    private static final String TEST_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String TEST_API_KEY = "test-sk-123456789abcdef";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatServiceImpl(
                restTemplate,
                userRepository,
                chatSessionRepository,
                chatMessageRepository,
                TEST_API_URL,
                TEST_API_KEY
        );
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
        assertThat(messages.get(0)).containsEntry("role", "user");
        assertThat(messages.get(0)).containsEntry("content", "What is Spring Boot?");
    }

    @Test
    @DisplayName("Persistence: should create new ChatSession and persist both user message and AI response when chatSessionId is null")
    @SuppressWarnings("unchecked")
    void shouldCreateNewSessionAndPersistBothMessagesWhenNoSessionId() {
        User user = new User("alice", "hash_alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        ChatSession savedSession = new ChatSession(101L, user, "Как работает Spring?", LocalDateTime.now());
        when(chatSessionRepository.save(any(ChatSession.class))).thenReturn(savedSession);

        String aiReplyJson = """
                {
                    "choices": [{ "message": { "content": "Spring управляет бинами через IoC контейнер." } }]
                }
                """;
        when(restTemplate.postForEntity(eq(TEST_API_URL), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(aiReplyJson));

        ChatRequestDto request = new ChatRequestDto("Как работает Spring?");
        ChatResponseDto response = chatService.processMessage(request, "alice");

        assertThat(response.chatSessionId()).isEqualTo(101L);
        assertThat(response.response()).isEqualTo("Spring управляет бинами через IoC контейнер.");

        // Проверяем создание сессии и последующее сохранение с ответом AI
        ArgumentCaptor<ChatSession> sessionCaptor = ArgumentCaptor.forClass(ChatSession.class);
        verify(chatSessionRepository, times(2)).save(sessionCaptor.capture());
        assertThat(sessionCaptor.getAllValues().get(0).getUser()).isEqualTo(user);
        assertThat(sessionCaptor.getAllValues().get(0).getTitle()).isEqualTo("Как работает Spring?");

        // Проверяем сохранение сообщений: 1) пользователя и 2) ответа AI
        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository, times(2)).save(messageCaptor.capture());

        List<ChatMessage> savedMessages = messageCaptor.getAllValues();
        assertThat(savedMessages.get(0).getSenderRole()).isEqualTo(ChatMessage.ROLE_USER);
        assertThat(savedMessages.get(0).getContent()).isEqualTo("Как работает Spring?");

        assertThat(savedMessages.get(1).getSenderRole()).isEqualTo(ChatMessage.ROLE_AI);
        assertThat(savedMessages.get(1).getContent()).isEqualTo("Spring управляет бинами через IoC контейнер.");
    }

    @Test
    @DisplayName("Persistence: should append messages to existing session when chatSessionId is provided")
    @SuppressWarnings("unchecked")
    void shouldAppendMessagesToExistingSessionWhenSessionIdProvided() {
        User user = new User("bob", "hash_bob");
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

        ChatSession existingSession = new ChatSession(202L, user, "Существующая беседа", LocalDateTime.now());
        when(chatSessionRepository.findByIdAndUser(202L, user)).thenReturn(Optional.of(existingSession));

        String aiReplyJson = """
                {
                    "choices": [{ "message": { "content": "PostgreSQL поддерживает транзакции ACID." } }]
                }
                """;
        when(restTemplate.postForEntity(eq(TEST_API_URL), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(aiReplyJson));

        ChatRequestDto request = new ChatRequestDto("Расскажи про PostgreSQL", 202L);
        ChatResponseDto response = chatService.processMessage(request, "bob");

        assertThat(response.chatSessionId()).isEqualTo(202L);
        assertThat(response.response()).isEqualTo("PostgreSQL поддерживает транзакции ACID.");

        // Сохранение сессии не должно создавать новую, а обновлять существующую
        verify(chatSessionRepository).findByIdAndUser(202L, user);
        verify(chatSessionRepository).save(existingSession);
        verify(chatMessageRepository, times(2)).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("Security: should throw ChatSessionNotFoundException when session does not belong to user")
    void shouldThrowExceptionWhenSessionBelongsToAnotherUser() {
        User user = new User("carol", "hash_carol");
        when(userRepository.findByUsername("carol")).thenReturn(Optional.of(user));
        when(chatSessionRepository.findByIdAndUser(999L, user)).thenReturn(Optional.empty());

        ChatRequestDto request = new ChatRequestDto("Попытка доступа к чужой сессии", 999L);

        assertThatThrownBy(() -> chatService.processMessage(request, "carol"))
                .isInstanceOf(ChatSessionNotFoundException.class)
                .hasMessageContaining("999")
                .hasMessageContaining("carol");

        verifyNoInteractions(restTemplate);
        verifyNoInteractions(chatMessageRepository);
    }

    @Test
    @DisplayName("Should handle HttpClientErrorException (401 Unauthorized) when API key is invalid")
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
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

    @Test
    @DisplayName("History: should return user chat sessions with message count and preview")
    void shouldReturnUserChatSessionsWithCountAndPreview() {
        User user = new User("alice", "hash_alice");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        ChatSession session1 = new ChatSession(1L, user, "Первая беседа", LocalDateTime.now().minusHours(2));
        ChatSession session2 = new ChatSession(2L, user, "Вторая беседа", LocalDateTime.now());

        when(chatSessionRepository.findByUserOrderByUpdatedAtDesc(user))
                .thenReturn(List.of(session2, session1));

        ChatMessage lastMsg2 = ChatMessage.aiMessage(session2, "Ответ на второй вопрос");
        ChatMessage lastMsg1 = ChatMessage.userMessage(session1, "Вопрос в первой беседе");

        when(chatMessageRepository.countBySession(session2)).thenReturn(4L);
        when(chatMessageRepository.countBySession(session1)).thenReturn(2L);
        when(chatMessageRepository.findFirstBySessionOrderByTimestampDesc(session2)).thenReturn(Optional.of(lastMsg2));
        when(chatMessageRepository.findFirstBySessionOrderByTimestampDesc(session1)).thenReturn(Optional.of(lastMsg1));

        List<ChatSessionResponseDto> sessions = chatService.getUserChatSessions("alice");

        assertThat(sessions).hasSize(2);
        assertThat(sessions.get(0).id()).isEqualTo(2L);
        assertThat(sessions.get(0).title()).isEqualTo("Вторая беседа");
        assertThat(sessions.get(0).messageCount()).isEqualTo(4L);
        assertThat(sessions.get(0).lastMessagePreview()).isEqualTo("Ответ на второй вопрос");

        assertThat(sessions.get(1).id()).isEqualTo(1L);
        assertThat(sessions.get(1).messageCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("History: should throw UserNotFoundException when fetching sessions for unknown user")
    void shouldThrowUserNotFoundExceptionWhenFetchingSessionsForUnknownUser() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getUserChatSessions("unknown"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("unknown");
    }

    @Test
    @DisplayName("History: should return chronological messages for owned session")
    void shouldReturnMessagesForOwnedSession() {
        User user = new User("bob", "hash_bob");
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

        ChatSession session = new ChatSession(10L, user, "Тестовый чат", LocalDateTime.now());
        when(chatSessionRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(session));

        ChatMessage m1 = ChatMessage.userMessage(session, "Привет!");
        ChatMessage m2 = ChatMessage.aiMessage(session, "Привет! Чем помочь?");
        when(chatMessageRepository.findBySessionOrderByTimestampAsc(session)).thenReturn(List.of(m1, m2));

        List<ChatMessageResponseDto> messages = chatService.getSessionMessages(10L, "bob");

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).senderRole()).isEqualTo("user");
        assertThat(messages.get(0).content()).isEqualTo("Привет!");
        assertThat(messages.get(1).senderRole()).isEqualTo("ai");
        assertThat(messages.get(1).content()).isEqualTo("Привет! Чем помочь?");
    }

    @Test
    @DisplayName("History: should throw ChatSessionNotFoundException when session belongs to another user (IDOR protection)")
    void shouldThrowChatSessionNotFoundExceptionWhenUserDoesNotOwnSession() {
        User user = new User("charlie", "hash_charlie");
        when(userRepository.findByUsername("charlie")).thenReturn(Optional.of(user));
        when(chatSessionRepository.findByIdAndUser(99L, user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatService.getSessionMessages(99L, "charlie"))
                .isInstanceOf(ChatSessionNotFoundException.class)
                .hasMessageContaining("99")
                .hasMessageContaining("charlie");

        verifyNoInteractions(chatMessageRepository);
    }

    @Test
    @DisplayName("History: should throw IllegalArgumentException when sessionId is null")
    void shouldThrowIllegalArgumentExceptionWhenSessionIdIsNull() {
        assertThatThrownBy(() -> chatService.getSessionMessages(null, "alice"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("null");
    }
}
