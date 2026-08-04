package com.example.demo.repository;

import com.example.demo.model.Conversation;
import com.example.demo.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * CRUD for {@link Conversation} via {@link JpaRepository}.
 * Custom finders return a user's conversations ordered by most recent activity.
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByUserOrderByCreatedAtDesc(User user);

    List<Conversation> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Conversation> findByIdAndUserId(Long id, Long userId);

    /**
     * Conversations for a user ordered by latest message time (falls back to conversation createdAt).
     */
    @Query("""
            SELECT c FROM Conversation c
            LEFT JOIN c.messages m
            WHERE c.user.id = :userId
            GROUP BY c
            ORDER BY COALESCE(MAX(m.createdAt), c.createdAt) DESC
            """)
    List<Conversation> findByUserIdOrderByLastActivityDesc(@Param("userId") Long userId);
}
