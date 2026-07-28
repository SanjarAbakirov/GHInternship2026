package com.example.demo.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.model.ChatMessage;
import com.example.demo.model.ChatSession;
import com.example.demo.model.User;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ChatSessionRepository;
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
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private User owner;
    private User otherUser;
    private ChatSession ownerSession;
    private ChatSession otherSession;
    private String ownerToken;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User("historyuser", "history@example.com", "password"));
        otherUser = userRepository.save(new User("stranger", "stranger@example.com", "password"));

        ownerSession = new ChatSession("Owner chat", owner);
        ownerSession.addMessage(new ChatMessage("Hi there", ChatMessage.ROLE_USER));
        ownerSession.addMessage(new ChatMessage("Hello back", ChatMessage.ROLE_AI));
        ownerSession = chatSessionRepository.save(ownerSession);

        ChatSession secondOwnerSession = chatSessionRepository.save(new ChatSession("Second chat", owner));

        otherSession = new ChatSession("Private chat", otherUser);
        otherSession.addMessage(new ChatMessage("secret", ChatMessage.ROLE_USER));
        otherSession = chatSessionRepository.save(otherSession);

        // touch second session so activity ordering is deterministic enough for presence checks
        chatMessageRepository.save(
                new ChatMessage("follow-up", ChatMessage.ROLE_USER, secondOwnerSession));

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
        mockMvc.perform(get("/api/chat/sessions/{sessionId}", ownerSession.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].content").value("Hi there"))
                .andExpect(jsonPath("$[0].role").value("user"))
                .andExpect(jsonPath("$[1].content").value("Hello back"))
                .andExpect(jsonPath("$[1].role").value("ai"));
    }

    @Test
    void getSessionMessages_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/chat/sessions/{sessionId}", ownerSession.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSessionMessages_otherUsersSession_returns404() throws Exception {
        mockMvc.perform(get("/api/chat/sessions/{sessionId}", otherSession.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNotFound());
    }
}
