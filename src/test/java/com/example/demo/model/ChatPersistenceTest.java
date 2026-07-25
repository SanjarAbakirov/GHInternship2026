package com.example.demo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ChatSessionRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ChatPersistenceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Test
    void cascadePersist_savesMessagesWithSession() {
        User owner = userRepository.save(new User("chatowner", "chat@example.com", "password"));

        ChatSession session = new ChatSession("Hello from user", owner);
        session.addMessage(new ChatMessage("Hello from user", ChatMessage.ROLE_USER));
        session.addMessage(new ChatMessage("Hello from AI", ChatMessage.ROLE_AI));

        ChatSession saved = chatSessionRepository.save(session);

        assertNotNull(saved.getId());
        assertEquals(2, chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(saved.getId()).size());
        assertEquals(ChatMessage.ROLE_USER,
                chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(saved.getId()).get(0).getRole());
    }

    @Test
    void cascadeDelete_removesMessagesWhenSessionDeleted() {
        User owner = userRepository.save(new User("deleter", "delete@example.com", "password"));

        ChatSession session = new ChatSession("To delete", owner);
        session.addMessage(new ChatMessage("msg", ChatMessage.ROLE_USER));
        ChatSession saved = chatSessionRepository.saveAndFlush(session);
        Long sessionId = saved.getId();

        assertFalse(chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).isEmpty());

        chatSessionRepository.delete(saved);
        chatSessionRepository.flush();

        assertTrue(chatSessionRepository.findById(sessionId).isEmpty());
        assertTrue(chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).isEmpty());
    }
}
