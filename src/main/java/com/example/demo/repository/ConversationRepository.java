package com.example.demo.repository;

import com.example.demo.model.Conversation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * CRUD for {@link Conversation} via {@link JpaRepository}.
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /** A user's conversations ordered by most-recently-updated first. */
    List<Conversation> findByUserIdOrderByUpdatedAtDesc(Long userId);

    /** Ownership-checked lookup: only returns a result if the conversation belongs to that user. */
    Optional<Conversation> findByIdAndUserId(Long id, Long userId);
}
