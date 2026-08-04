package com.example.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.model.Conversation;
import com.example.demo.model.Message;
import com.example.demo.model.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ConversationRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    private User owner;
    private User otherUser;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User("sessionowner", "owner@example.com", "password"));
        otherUser = userRepository.save(new User("otheruser", "other@example.com", "password"));
    }

    @Test
    void saveAndFindById_persistsConversationForUser() {
        Conversation conversation = conversationRepository.save(new Conversation("My first chat", owner));

        Optional<Conversation> found = conversationRepository.findById(conversation.getId());

        assertTrue(found.isPresent());
        assertEquals("My first chat", found.get().getTitle());
        assertEquals(owner.getId(), found.get().getUser().getId());
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_returnsOnlyOwnerConversationsNewestFirst()
            throws InterruptedException {
        Conversation older = conversationRepository.saveAndFlush(new Conversation("Older", owner));
        Thread.sleep(20);
        Conversation newer = conversationRepository.saveAndFlush(new Conversation("Newer", owner));
        conversationRepository.saveAndFlush(new Conversation("Other user's chat", otherUser));

        List<Conversation> conversations = conversationRepository.findByUserIdOrderByCreatedAtDesc(owner.getId());

        assertEquals(2, conversations.size());
        assertEquals(newer.getId(), conversations.get(0).getId());
        assertEquals(older.getId(), conversations.get(1).getId());
    }

    @Test
    void findByUserOrderByCreatedAtDesc_filtersByUserEntity() {
        conversationRepository.save(new Conversation("Owned", owner));
        conversationRepository.save(new Conversation("Not owned", otherUser));

        List<Conversation> conversations = conversationRepository.findByUserOrderByCreatedAtDesc(owner);

        assertEquals(1, conversations.size());
        assertEquals("Owned", conversations.get(0).getTitle());
    }

    @Test
    void findByIdAndUserId_returnsConversationOnlyWhenOwned() {
        Conversation owned = conversationRepository.save(new Conversation("Owned", owner));
        Conversation foreign = conversationRepository.save(new Conversation("Foreign", otherUser));

        assertTrue(conversationRepository.findByIdAndUserId(owned.getId(), owner.getId()).isPresent());
        assertTrue(conversationRepository.findByIdAndUserId(foreign.getId(), owner.getId()).isEmpty());
        assertTrue(conversationRepository.findByIdAndUserId(owned.getId(), otherUser.getId()).isEmpty());
    }

    @Test
    void findByUserIdOrderByLastActivityDesc_ordersByLatestMessageActivity()
            throws InterruptedException {
        Conversation quiet = conversationRepository.saveAndFlush(new Conversation("Quiet", owner));

        Conversation active = new Conversation("Active", owner);
        active.addMessage(new Message("hello", Message.ROLE_USER));
        active = conversationRepository.saveAndFlush(active);

        Thread.sleep(20);

        quiet.addMessage(new Message("later activity", Message.ROLE_USER));
        quiet = conversationRepository.saveAndFlush(quiet);

        List<Conversation> conversations =
                conversationRepository.findByUserIdOrderByLastActivityDesc(owner.getId());

        assertEquals(2, conversations.size());
        assertEquals(quiet.getId(), conversations.get(0).getId());
        assertEquals(active.getId(), conversations.get(1).getId());
    }
}
