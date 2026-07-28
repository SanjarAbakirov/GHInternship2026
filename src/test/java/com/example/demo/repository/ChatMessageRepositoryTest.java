package com.example.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.model.ChatMessage;
import com.example.demo.model.ChatSession;
import com.example.demo.model.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ChatMessageRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private ChatSession session;

    @BeforeEach
    void setUp() {
        User owner = userRepository.save(new User("msgowner", "msg@example.com", "password"));
        session = chatSessionRepository.save(new ChatSession("Message session", owner));
    }

    @Test
    void save_persistsMessageLinkedToSession() {
        ChatMessage saved = chatMessageRepository.save(
                new ChatMessage("Hello", ChatMessage.ROLE_USER, session));

        ChatMessage found = chatMessageRepository.findById(saved.getId()).orElseThrow();

        assertEquals("Hello", found.getContent());
        assertEquals(ChatMessage.ROLE_USER, found.getRole());
        assertEquals(session.getId(), found.getSession().getId());
        assertEquals(1, chatMessageRepository.countBySessionId(session.getId()));
    }

    @Test
    void findBySessionIdOrderByCreatedAtAsc_returnsChronologicalMessages()
            throws InterruptedException {
        ChatMessage first = chatMessageRepository.saveAndFlush(
                new ChatMessage("first", ChatMessage.ROLE_USER, session));
        Thread.sleep(20);
        ChatMessage second = chatMessageRepository.saveAndFlush(
                new ChatMessage("second", ChatMessage.ROLE_AI, session));

        List<ChatMessage> messages =
                chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());

        assertEquals(2, messages.size());
        assertEquals(first.getId(), messages.get(0).getId());
        assertEquals(second.getId(), messages.get(1).getId());
        assertEquals(ChatMessage.ROLE_USER, messages.get(0).getRole());
        assertEquals(ChatMessage.ROLE_AI, messages.get(1).getRole());
    }

    @Test
    void findBySessionOrderByCreatedAtAsc_returnsMessagesForSessionEntity() {
        chatMessageRepository.save(new ChatMessage("via entity", ChatMessage.ROLE_USER, session));

        List<ChatMessage> messages = chatMessageRepository.findBySessionOrderByCreatedAtAsc(session);

        assertEquals(1, messages.size());
        assertEquals("via entity", messages.get(0).getContent());
    }

    @Test
    void deleteBySessionId_removesAllMessagesForSession() {
        chatMessageRepository.save(new ChatMessage("one", ChatMessage.ROLE_USER, session));
        chatMessageRepository.save(new ChatMessage("two", ChatMessage.ROLE_AI, session));
        assertEquals(2, chatMessageRepository.countBySessionId(session.getId()));

        chatMessageRepository.deleteBySessionId(session.getId());

        assertEquals(0, chatMessageRepository.countBySessionId(session.getId()));
        assertTrue(chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()).isEmpty());
    }
}
