package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.dto.ChatResponse;
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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class ChatServiceTest {

    private RestTemplate restTemplateMock;
    private UserRepository userRepository;
    private ChatSessionRepository chatSessionRepository;
    private ChatMessageRepository chatMessageRepository;
    private ChatService chatService;
    private User testUser;

    @BeforeEach
    public void setUp() {
        restTemplateMock = Mockito.mock(RestTemplate.class);
        userRepository = Mockito.mock(UserRepository.class);
        chatSessionRepository = Mockito.mock(ChatSessionRepository.class);
        chatMessageRepository = Mockito.mock(ChatMessageRepository.class);

        chatService = new ChatService(
                restTemplateMock,
                userRepository,
                chatSessionRepository,
                chatMessageRepository);

        ReflectionTestUtils.setField(chatService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(chatService, "apiUrl", "http://test.api.url");
        ReflectionTestUtils.setField(chatService, "apiModel", "gpt-3.5-turbo");

        testUser = new User("testuser", "test@example.com", "password");
        testUser.setId(1L);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    public void chat_createsNewSessionAndPersistsBothMessages() {
        stubAiReply("Hello from AI");

        when(chatSessionRepository.save(any(ChatSession.class))).thenAnswer(invocation -> {
            ChatSession session = invocation.getArgument(0);
            session.setId(10L);
            return session;
        });
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatResponse response = chatService.chat("testuser", "Hello", null);

        assertEquals("Hello from AI", response.getReply());
        assertEquals(10L, response.getChatSessionId());

        ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageRepository, Mockito.times(2)).save(messageCaptor.capture());
        List<ChatMessage> savedMessages = messageCaptor.getAllValues();
        assertEquals(ChatMessage.ROLE_USER, savedMessages.get(0).getRole());
        assertEquals("Hello", savedMessages.get(0).getContent());
        assertEquals(ChatMessage.ROLE_AI, savedMessages.get(1).getRole());
        assertEquals("Hello from AI", savedMessages.get(1).getContent());
    }

    @Test
    public void chat_usesExistingSessionWhenIdProvided() {
        stubAiReply("Follow-up reply");

        ChatSession existing = new ChatSession("Existing", testUser);
        existing.setId(22L);
        when(chatSessionRepository.findByIdAndUserId(22L, 1L)).thenReturn(Optional.of(existing));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatResponse response = chatService.chat("testuser", "Next message", 22L);

        assertEquals(22L, response.getChatSessionId());
        assertEquals("Follow-up reply", response.getReply());
        verify(chatSessionRepository, Mockito.never()).save(any(ChatSession.class));
        verify(chatMessageRepository, Mockito.times(2)).save(any(ChatMessage.class));
    }

    @Test
    public void chat_throwsWhenSessionMissingOrNotOwned() {
        when(chatSessionRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> chatService.chat("testuser", "Hello", 99L));
    }

    @Test
    public void getChatReply_success() {
        stubAiReply("Hello from AI");
        assertEquals("Hello from AI", chatService.getChatReply("Hello"));
    }

    @Test
    public void getChatReply_error() {
        when(restTemplateMock.postForEntity(
                ArgumentMatchers.eq("http://test.api.url"),
                ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(Map.class)
        )).thenThrow(new RestClientException("Connection refused"));

        AiServiceException exception = assertThrows(AiServiceException.class, () -> {
            chatService.getChatReply("Hello");
        });

        assertEquals("AI service is currently unavailable. Please try again later.", exception.getMessage());
    }

    private void stubAiReply(String reply) {
        Map<String, Object> messageMap = Map.of("content", reply);
        Map<String, Object> choiceMap = Map.of("message", messageMap);
        Map<String, Object> responseBody = Map.of("choices", List.of(choiceMap));
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        when(restTemplateMock.postForEntity(
                ArgumentMatchers.eq("http://test.api.url"),
                ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(Map.class)
        )).thenReturn(responseEntity);
    }
}
