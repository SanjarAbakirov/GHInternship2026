package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.model.Conversation;
import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Step 7.4: full-stack end-to-end coverage of {@code POST /api/chat} -- real service, real
 * repositories, real (in-memory) database. Complements {@link ChatHistoryIntegrationTest}, which
 * covers the list/detail/delete endpoints, and {@link ChatControllerTest}, which mocks the service
 * layer to test the controller in isolation.
 *
 * <p>{@code openai.api.url} in {@code application-test.properties} points at an unreachable host,
 * so every AI call falls back to {@link com.example.demo.service.ChatService}'s offline mock reply
 * -- deterministic and fast, while still exercising the full persistence path.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ChatFlowIntegrationTest {

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

    @Autowired
    private ObjectMapper objectMapper;

    private User owner;
    private User otherUser;
    private String ownerToken;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User("flowuser", "flow@example.com", "password"));
        otherUser = userRepository.save(new User("flowstranger", "flowstranger@example.com", "password"));
        ownerToken = jwtUtil.generateToken(owner.getUsername());
    }

    @Test
    void postChat_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Hi\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postChat_newConversation_persistsUserAndAiMessages() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/chat")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Hello there\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newConversation").value(true))
                .andExpect(jsonPath("$.conversationTitle").value("Hello there"))
                .andExpect(jsonPath("$.reply").isNotEmpty())
                .andReturn();

        long conversationId = conversationIdFrom(result);

        List<Conversation> conversations = conversationRepository.findByUserIdOrderByUpdatedAtDesc(owner.getId());
        assertEquals(1, conversations.size());
        assertEquals(conversationId, conversations.get(0).getId());

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        assertEquals(2, messages.size());
        assertEquals(Message.ROLE_USER, messages.get(0).getRole());
        assertEquals("Hello there", messages.get(0).getContent());
        assertEquals(Message.ROLE_AI, messages.get(1).getRole());
    }

    @Test
    void postChat_existingConversation_appendsMessagesInsteadOfCreatingNew() throws Exception {
        MvcResult first = mockMvc.perform(post("/api/chat")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"First message\"}"))
                .andExpect(status().isOk())
                .andReturn();
        long conversationId = conversationIdFrom(first);

        mockMvc.perform(post("/api/chat")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Second message\",\"conversationId\":" + conversationId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(conversationId))
                .andExpect(jsonPath("$.newConversation").value(false));

        assertEquals(1, conversationRepository.findByUserIdOrderByUpdatedAtDesc(owner.getId()).size());
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        assertEquals(4, messages.size());
        assertEquals("First message", messages.get(0).getContent());
        assertEquals("Second message", messages.get(2).getContent());
    }

    @Test
    void postChat_otherUsersConversationId_returns403AndPersistsNothing() throws Exception {
        Conversation foreign = conversationRepository.save(new Conversation("Foreign chat", otherUser));

        mockMvc.perform(post("/api/chat")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Hi\",\"conversationId\":" + foreign.getId() + "}"))
                .andExpect(status().isForbidden());

        assertEquals(0, messageRepository.findByConversationIdOrderByCreatedAtAsc(foreign.getId()).size());
    }

    @Test
    void postChat_missingConversationId_returns404() throws Exception {
        long missingId = 999_999L;

        mockMvc.perform(post("/api/chat")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Hi\",\"conversationId\":" + missingId + "}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void postChat_blankMessage_returns400() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    private long conversationIdFrom(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("conversationId").asLong();
    }
}
