package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.dto.ChatResponse;
import com.example.demo.dto.ConversationDetailResponse;
import com.example.demo.dto.ConversationResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Conversation;
import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDateTime;
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
    private ConversationRepository conversationRepository;
    private MessageRepository messageRepository;
    private ChatService chatService;
    private User testUser;

    @BeforeEach
    public void setUp() {
        restTemplateMock = Mockito.mock(RestTemplate.class);
        userRepository = Mockito.mock(UserRepository.class);
        conversationRepository = Mockito.mock(ConversationRepository.class);
        messageRepository = Mockito.mock(MessageRepository.class);

        chatService = new ChatService(
                restTemplateMock,
                userRepository,
                conversationRepository,
                messageRepository);

        ReflectionTestUtils.setField(chatService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(chatService, "apiUrl", "http://test.api.url");
        ReflectionTestUtils.setField(chatService, "apiModel", "gpt-3.5-turbo");

        testUser = new User("testuser", "test@example.com", "password");
        testUser.setId(1L);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
    }

    @Test
    public void chat_createsNewConversationAndPersistsBothMessages() {
        stubAiReply("Hello from AI");

        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> {
            Conversation conversation = invocation.getArgument(0);
            conversation.setId(10L);
            return conversation;
        });
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message message = invocation.getArgument(0);
            message.setCreatedAt(LocalDateTime.now());
            return message;
        });

        ChatResponse response = chatService.chat("testuser", "Hello", null, null);

        assertEquals("Hello from AI", response.getReply());
        assertEquals(10L, response.getConversationId());
        assertEquals("Hello", response.getConversationTitle());
        assertEquals(true, response.isNewConversation());
        assertEquals(true, response.getTimestamp() != null);

        // Saved once when the conversation is created, and again at the end of chat()
        // to persist the updatedAt bump from addMessage().
        ArgumentCaptor<Conversation> conversationCaptor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepository, Mockito.times(2)).save(conversationCaptor.capture());
        Conversation savedConversation = conversationCaptor.getValue();
        assertEquals("Hello", savedConversation.getTitle());
        assertEquals(testUser.getId(), savedConversation.getUser().getId());
        assertEquals("gpt-3.5-turbo", savedConversation.getModelName());

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository, Mockito.times(2)).save(messageCaptor.capture());
        List<Message> savedMessages = messageCaptor.getAllValues();
        assertEquals(Message.ROLE_USER, savedMessages.get(0).getRole());
        assertEquals("Hello", savedMessages.get(0).getContent());
        assertEquals(10L, savedMessages.get(0).getConversation().getId());
        assertEquals(Message.ROLE_AI, savedMessages.get(1).getRole());
        assertEquals("Hello from AI", savedMessages.get(1).getContent());
        assertEquals(10L, savedMessages.get(1).getConversation().getId());
    }

    @Test
    public void chat_usesExistingConversationWhenIdProvided() {
        stubAiReply("Follow-up reply");

        Conversation existing = new Conversation("Existing", testUser);
        existing.setId(22L);
        when(conversationRepository.findByIdAndUserId(22L, 1L)).thenReturn(Optional.of(existing));
        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatResponse response = chatService.chat("testuser", "Next message", 22L, null);

        assertEquals(22L, response.getConversationId());
        assertEquals("Follow-up reply", response.getReply());
        assertEquals(false, response.isNewConversation());
        // No new conversation is created, but it's still saved once to persist the updatedAt bump.
        verify(conversationRepository, Mockito.times(1)).save(existing);

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository, Mockito.times(2)).save(messageCaptor.capture());
        List<Message> savedMessages = messageCaptor.getAllValues();
        assertEquals("Next message", savedMessages.get(0).getContent());
        assertEquals(Message.ROLE_USER, savedMessages.get(0).getRole());
        assertEquals(existing.getId(), savedMessages.get(0).getConversation().getId());
        assertEquals("Follow-up reply", savedMessages.get(1).getContent());
        assertEquals(Message.ROLE_AI, savedMessages.get(1).getRole());
        assertEquals(existing.getId(), savedMessages.get(1).getConversation().getId());
    }

    @Test
    public void chat_throwsWhenConversationMissingOrNotOwned() {
        when(conversationRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> chatService.chat("testuser", "Hello", 99L, null));
    }

    @Test
    public void chat_usesModelNameOverrideWhenCreatingNewConversation() {
        stubAiReply("Hello from AI");

        when(conversationRepository.save(any(Conversation.class))).thenAnswer(invocation -> {
            Conversation conversation = invocation.getArgument(0);
            conversation.setId(11L);
            return conversation;
        });
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        chatService.chat("testuser", "Hello", null, "custom-model");

        ArgumentCaptor<Conversation> conversationCaptor = ArgumentCaptor.forClass(Conversation.class);
        verify(conversationRepository, Mockito.times(2)).save(conversationCaptor.capture());
        assertEquals("custom-model", conversationCaptor.getAllValues().get(0).getModelName());
    }

    @Test
    public void listSessions_returnsConversationsOrderedByLastActivity() {
        Conversation first = new Conversation("First", testUser);
        first.setId(1L);
        first.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        Conversation second = new Conversation("Second", testUser);
        second.setId(2L);
        second.setCreatedAt(LocalDateTime.of(2026, 1, 2, 10, 0));

        when(conversationRepository.findByUserIdOrderByUpdatedAtDesc(1L))
                .thenReturn(List.of(second, first));
        when(messageRepository.countByConversationId(1L)).thenReturn(2L);
        when(messageRepository.countByConversationId(2L)).thenReturn(0L);
        when(messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.empty());

        List<ConversationResponse> sessions = chatService.listSessions("testuser");

        assertEquals(2, sessions.size());
        assertEquals(2L, sessions.get(0).getId());
        assertEquals("Second", sessions.get(0).getTitle());
        assertEquals(1L, sessions.get(1).getId());
        assertEquals(2L, sessions.get(1).getMessageCount());
    }

    @Test
    public void getConversationDetail_returnsMetadataAndMessagesForOwnedConversation() {
        Conversation conversation = new Conversation("Owned", testUser);
        conversation.setId(7L);
        conversation.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        conversation.setUpdatedAt(LocalDateTime.of(2026, 1, 1, 12, 1));
        when(conversationRepository.findByIdAndUserId(7L, 1L)).thenReturn(Optional.of(conversation));

        Message userMsg = new Message("Hi", Message.ROLE_USER, conversation);
        userMsg.setId(100L);
        userMsg.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        Message aiMsg = new Message("Hello", Message.ROLE_AI, conversation);
        aiMsg.setId(101L);
        aiMsg.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 1));
        when(messageRepository.findByConversationIdOrderByCreatedAtAsc(7L))
                .thenReturn(List.of(userMsg, aiMsg));

        ConversationDetailResponse detail = chatService.getConversationDetail("testuser", 7L);

        assertEquals(7L, detail.getId());
        assertEquals("Owned", detail.getTitle());
        assertEquals(2, detail.getMessageCount());
        assertEquals(2, detail.getMessages().size());
        assertEquals("Hi", detail.getMessages().get(0).getContent());
        assertEquals(Message.ROLE_USER, detail.getMessages().get(0).getRole());
        assertEquals("Hello", detail.getMessages().get(1).getContent());
        assertEquals(Message.ROLE_AI, detail.getMessages().get(1).getRole());
    }

    @Test
    public void getConversationDetail_throwsWhenConversationNotOwned() {
        when(conversationRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> chatService.getConversationDetail("testuser", 99L));
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

        String reply = chatService.getChatReply("Hello");
        assertEquals("AI (Offline Mock): Внешний API провайдер недоступен или API-ключ OpenAI истек. Ваше сообщение сохранены в сессии: \"Hello\"", reply);
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
