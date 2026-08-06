package com.example.demo.repository;

import com.example.demo.model.Message;
import java.util.List;
import java.util.Optional;
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

    /** Total message count, used for the optional {@code messageCount} field in conversation list DTOs. */
    long countByConversationId(Long conversationId);

    /** Most recent message, used for the optional {@code lastMessagePreview} field in conversation list DTOs. */
    Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(Long conversationId);
}
