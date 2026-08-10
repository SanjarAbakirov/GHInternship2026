package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Step 7.4: exercises {@code POST /api/chat} under concurrent load against the real (in-memory)
 * database. Deliberately NOT {@code @Transactional} -- each simulated client request needs its own
 * committed transaction on its own pooled connection, the same as it would in production, rather
 * than sharing the single connection a test-rollback transaction would pin it to.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatConcurrencyIntegrationTest {

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

    private User user;
    private String token;

    @BeforeEach
    void setUp() {
        user = userRepository.save(new User("concurrencyuser", "concurrency@example.com", "password"));
        token = jwtUtil.generateToken(user.getUsername());
    }

    @AfterEach
    void tearDown() {
        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void concurrentNewConversationRequests_eachCreatesAnIsolatedConversation() throws Exception {
        int threadCount = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < threadCount; i++) {
                int idx = i;
                futures.add(executor.submit(() -> {
                    start.await();
                    MvcResult result = mockMvc.perform(post("/api/chat")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"message\":\"Concurrent message " + idx + "\"}"))
                            .andReturn();
                    return result.getResponse().getStatus();
                }));
            }

            start.countDown();
            for (Future<Integer> future : futures) {
                assertEquals(200, future.get(15, TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdown();
        }

        List<Conversation> conversations = conversationRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());
        assertEquals(threadCount, conversations.size(), "each concurrent request should create its own conversation");

        Set<Long> uniqueIds = conversations.stream().map(Conversation::getId).collect(Collectors.toSet());
        assertEquals(threadCount, uniqueIds.size(), "conversation ids must not collide under concurrent inserts");

        for (Conversation conversation : conversations) {
            List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());
            assertEquals(2, messages.size(), "each conversation should have exactly its own user+AI message pair");
        }
    }

    @Test
    void concurrentMessagesOnSameConversation_allPersistWithoutLoss() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/chat")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Starting message\"}"))
                .andExpect(status().isOk())
                .andReturn();
        long conversationId = conversationIdFrom(created);

        int threadCount = 6;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < threadCount; i++) {
                int idx = i;
                futures.add(executor.submit(() -> {
                    start.await();
                    MvcResult result = mockMvc.perform(post("/api/chat")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"message\":\"Follow-up " + idx + "\",\"conversationId\":"
                                            + conversationId + "}"))
                            .andReturn();
                    return result.getResponse().getStatus();
                }));
            }

            start.countDown();
            for (Future<Integer> future : futures) {
                assertEquals(200, future.get(15, TimeUnit.SECONDS));
            }
        } finally {
            executor.shutdown();
        }

        // 2 messages from the initial creation + 2 (user+AI) per concurrent follow-up, with no
        // lost writes despite ChatService.chat() not being wrapped in a single DB transaction.
        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        assertEquals(2 + threadCount * 2, messages.size());

        long userMessages = messages.stream().filter(m -> Message.ROLE_USER.equals(m.getRole())).count();
        long aiMessages = messages.stream().filter(m -> Message.ROLE_AI.equals(m.getRole())).count();
        assertEquals(1 + threadCount, userMessages);
        assertEquals(1 + threadCount, aiMessages);

        assertTrue(conversationRepository.findById(conversationId).isPresent());
    }

    private long conversationIdFrom(MvcResult result) throws Exception {
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("conversationId").asLong();
    }
}
