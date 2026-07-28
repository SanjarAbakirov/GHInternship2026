package com.example.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.model.ChatMessage;
import com.example.demo.model.ChatSession;
import com.example.demo.model.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ChatSessionRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    private User owner;
    private User otherUser;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User("sessionowner", "owner@example.com", "password"));
        otherUser = userRepository.save(new User("otheruser", "other@example.com", "password"));
    }

    @Test
    void saveAndFindById_persistsSessionForUser() {
        ChatSession session = chatSessionRepository.save(new ChatSession("My first chat", owner));

        Optional<ChatSession> found = chatSessionRepository.findById(session.getId());

        assertTrue(found.isPresent());
        assertEquals("My first chat", found.get().getTitle());
        assertEquals(owner.getId(), found.get().getUser().getId());
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_returnsOnlyOwnerSessionsNewestFirst()
            throws InterruptedException {
        ChatSession older = chatSessionRepository.saveAndFlush(new ChatSession("Older", owner));
        Thread.sleep(20);
        ChatSession newer = chatSessionRepository.saveAndFlush(new ChatSession("Newer", owner));
        chatSessionRepository.saveAndFlush(new ChatSession("Other user's chat", otherUser));

        List<ChatSession> sessions = chatSessionRepository.findByUserIdOrderByCreatedAtDesc(owner.getId());

        assertEquals(2, sessions.size());
        assertEquals(newer.getId(), sessions.get(0).getId());
        assertEquals(older.getId(), sessions.get(1).getId());
    }

    @Test
    void findByUserOrderByCreatedAtDesc_filtersByUserEntity() {
        chatSessionRepository.save(new ChatSession("Owned", owner));
        chatSessionRepository.save(new ChatSession("Not owned", otherUser));

        List<ChatSession> sessions = chatSessionRepository.findByUserOrderByCreatedAtDesc(owner);

        assertEquals(1, sessions.size());
        assertEquals("Owned", sessions.get(0).getTitle());
    }

    @Test
    void findByIdAndUserId_returnsSessionOnlyWhenOwned() {
        ChatSession owned = chatSessionRepository.save(new ChatSession("Owned", owner));
        ChatSession foreign = chatSessionRepository.save(new ChatSession("Foreign", otherUser));

        assertTrue(chatSessionRepository.findByIdAndUserId(owned.getId(), owner.getId()).isPresent());
        assertTrue(chatSessionRepository.findByIdAndUserId(foreign.getId(), owner.getId()).isEmpty());
        assertTrue(chatSessionRepository.findByIdAndUserId(owned.getId(), otherUser.getId()).isEmpty());
    }

    @Test
    void findByUserIdOrderByLastActivityDesc_ordersByLatestMessageActivity()
            throws InterruptedException {
        ChatSession quiet = chatSessionRepository.saveAndFlush(new ChatSession("Quiet", owner));

        ChatSession active = new ChatSession("Active", owner);
        active.addMessage(new ChatMessage("hello", ChatMessage.ROLE_USER));
        active = chatSessionRepository.saveAndFlush(active);

        Thread.sleep(20);

        quiet.addMessage(new ChatMessage("later activity", ChatMessage.ROLE_USER));
        quiet = chatSessionRepository.saveAndFlush(quiet);

        List<ChatSession> sessions =
                chatSessionRepository.findByUserIdOrderByLastActivityDesc(owner.getId());

        assertEquals(2, sessions.size());
        assertEquals(quiet.getId(), sessions.get(0).getId());
        assertEquals(active.getId(), sessions.get(1).getId());
    }
}
