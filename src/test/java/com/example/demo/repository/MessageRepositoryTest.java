package com.example.demo.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.demo.model.Conversation;
import com.example.demo.model.Message;
import com.example.demo.model.User;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class MessageRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    private Conversation conversation;

    @BeforeEach
    void setUp() {
        User owner = userRepository.save(new User("msgowner", "msg@example.com", "password"));
        conversation = conversationRepository.save(new Conversation("Message conversation", owner));
    }

    @Test
    void save_persistsMessageLinkedToConversation() {
        Message saved = messageRepository.save(
                new Message("Hello", Message.ROLE_USER, conversation));

        Message found = messageRepository.findById(saved.getId()).orElseThrow();

        assertEquals("Hello", found.getContent());
        assertEquals(Message.ROLE_USER, found.getRole());
        assertEquals(conversation.getId(), found.getConversation().getId());
    }

    @Test
    void findByConversationIdOrderByCreatedAtAsc_returnsChronologicalMessages()
            throws InterruptedException {
        Message first = messageRepository.saveAndFlush(
                new Message("first", Message.ROLE_USER, conversation));
        Thread.sleep(20);
        Message second = messageRepository.saveAndFlush(
                new Message("second", Message.ROLE_AI, conversation));

        List<Message> messages =
                messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());

        assertEquals(2, messages.size());
        assertEquals(first.getId(), messages.get(0).getId());
        assertEquals(second.getId(), messages.get(1).getId());
        assertEquals(Message.ROLE_USER, messages.get(0).getRole());
        assertEquals(Message.ROLE_AI, messages.get(1).getRole());
    }

    @Test
    void deleteByConversationId_removesAllMessagesForConversation() {
        messageRepository.save(new Message("one", Message.ROLE_USER, conversation));
        messageRepository.save(new Message("two", Message.ROLE_AI, conversation));
        assertEquals(2, messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId()).size());

        messageRepository.deleteByConversationId(conversation.getId());

        assertTrue(messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId()).isEmpty());
    }
}
