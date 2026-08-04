package com.example.demo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.repository.ConversationRepository;
import com.example.demo.repository.MessageRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class ConversationPersistenceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Test
    void cascadePersist_savesMessagesWithConversation() {
        User owner = userRepository.save(new User("chatowner", "chat@example.com", "password"));

        Conversation conversation = new Conversation("Hello from user", owner);
        conversation.addMessage(new Message("Hello from user", Message.ROLE_USER));
        conversation.addMessage(new Message("Hello from AI", Message.ROLE_AI));

        Conversation saved = conversationRepository.save(conversation);

        assertNotNull(saved.getId());
        assertEquals(2, messageRepository.findByConversationIdOrderByCreatedAtAsc(saved.getId()).size());
        assertEquals(Message.ROLE_USER,
                messageRepository.findByConversationIdOrderByCreatedAtAsc(saved.getId()).get(0).getRole());
    }

    @Test
    void cascadeDelete_removesMessagesWhenConversationDeleted() {
        User owner = userRepository.save(new User("deleter", "delete@example.com", "password"));

        Conversation conversation = new Conversation("To delete", owner);
        conversation.addMessage(new Message("msg", Message.ROLE_USER));
        Conversation saved = conversationRepository.saveAndFlush(conversation);
        Long conversationId = saved.getId();

        assertFalse(messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).isEmpty());

        conversationRepository.delete(saved);
        conversationRepository.flush();

        assertTrue(conversationRepository.findById(conversationId).isEmpty());
        assertTrue(messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId).isEmpty());
    }

    @Test
    void findByUserIdOrderByLastActivityDesc_returnsMostRecentlyActiveFirst() throws InterruptedException {
        User owner = userRepository.save(new User("activeuser", "active@example.com", "password"));

        Conversation older = new Conversation("Older conversation", owner);
        older.addMessage(new Message("first", Message.ROLE_USER));
        older = conversationRepository.saveAndFlush(older);

        Thread.sleep(20);

        Conversation newer = new Conversation("Newer conversation", owner);
        newer.addMessage(new Message("later", Message.ROLE_USER));
        newer = conversationRepository.saveAndFlush(newer);

        var conversations = conversationRepository.findByUserIdOrderByLastActivityDesc(owner.getId());

        assertEquals(2, conversations.size());
        assertEquals(newer.getId(), conversations.get(0).getId());
        assertEquals(older.getId(), conversations.get(1).getId());
        assertEquals(1, messageRepository.countByConversationId(newer.getId()));
    }
}
