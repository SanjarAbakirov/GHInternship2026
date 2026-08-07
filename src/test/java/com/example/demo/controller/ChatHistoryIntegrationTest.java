package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.model.Conversation;
import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Full-stack integration tests for chat history endpoints against in-memory H2.
 * Verifies JWT security and that only the authenticated user's data is returned.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ChatHistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    private User owner;
    private User otherUser;
    private Conversation ownerConversation;
    private Conversation otherConversation;
    private String ownerToken;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User("historyuser", "history@example.com", "password"));
        otherUser = userRepository.save(new User("stranger", "stranger@example.com", "password"));

        ownerConversation = new Conversation("Owner chat", owner);
        ownerConversation.addMessage(new Message("Hi there", Message.ROLE_USER));
        ownerConversation.addMessage(new Message("Hello back", Message.ROLE_AI));
        ownerConversation = conversationRepository.save(ownerConversation);

        Conversation secondOwnerConversation = conversationRepository.save(new Conversation("Second chat", owner));

        otherConversation = new Conversation("Private chat", otherUser);
        otherConversation.addMessage(new Message("secret", Message.ROLE_USER));
        otherConversation = conversationRepository.save(otherConversation);

        // touch second conversation so activity ordering is deterministic enough for presence checks
        messageRepository.save(
                new Message("follow-up", Message.ROLE_USER, secondOwnerConversation));

        ownerToken = jwtUtil.generateToken(owner.getUsername());
    }

    @Test
    void listSessions_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/chat/sessions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listSessions_authenticated_returnsOnlyOwnSessions() throws Exception {
        mockMvc.perform(get("/api/chat/sessions")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.title=='Owner chat')]").exists())
                .andExpect(jsonPath("$[?(@.title=='Second chat')]").exists())
                .andExpect(jsonPath("$[?(@.title=='Private chat')]").doesNotExist());
    }

    @Test
    void getSessionMessages_authenticated_returnsMessagesForOwnedSession() throws Exception {
        mockMvc.perform(get("/api/chat/sessions/{sessionId}", ownerConversation.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ownerConversation.getId()))
                .andExpect(jsonPath("$.title").value("Owner chat"))
                .andExpect(jsonPath("$.messageCount").value(2))
                .andExpect(jsonPath("$.messages.length()").value(2))
                .andExpect(jsonPath("$.messages[0].content").value("Hi there"))
                .andExpect(jsonPath("$.messages[0].role").value("user"))
                .andExpect(jsonPath("$.messages[1].content").value("Hello back"))
                .andExpect(jsonPath("$.messages[1].role").value("ai"));
    }

    @Test
    void getSessionMessages_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/chat/sessions/{sessionId}", ownerConversation.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSessionMessages_otherUsersSession_returns404() throws Exception {
        mockMvc.perform(get("/api/chat/sessions/{sessionId}", otherConversation.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteSession_authenticated_deletesConversationAndCascadesMessages() throws Exception {
        Long conversationId = ownerConversation.getId();
        assertTrue(messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).size() > 0);

        mockMvc.perform(delete("/api/chat/sessions/{sessionId}", conversationId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());

        assertTrue(conversationRepository.findByIdAndUserId(conversationId, owner.getId()).isEmpty());
        assertTrue(messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).isEmpty());
    }

    @Test
    void deleteSession_otherUsersSession_returns404AndDoesNotDelete() throws Exception {
        mockMvc.perform(delete("/api/chat/sessions/{sessionId}", otherConversation.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNotFound());

        assertTrue(conversationRepository.findByIdAndUserId(otherConversation.getId(), otherUser.getId()).isPresent());
    }

    @Test
    void deleteSession_unauthenticated_returns401() throws Exception {
        mockMvc.perform(delete("/api/chat/sessions/{sessionId}", ownerConversation.getId()))
                .andExpect(status().isUnauthorized());
    }
}
