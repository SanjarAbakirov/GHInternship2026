package com.example.demo.repository;

import com.example.demo.model.Message;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * CRUD for {@link Message} via {@link JpaRepository}.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /** Messages in a conversation, oldest first. */
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    /** Clears a conversation's message history without deleting the conversation itself. */
    void deleteByConversationId(Long conversationId);
}
