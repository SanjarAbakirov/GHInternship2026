package com.example.demo.repository;

import com.example.demo.model.ChatSession;
import com.example.demo.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * CRUD for {@link ChatSession} via {@link JpaRepository}.
 * Custom finders return a user's sessions ordered by most recent activity.
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    List<ChatSession> findByUserOrderByCreatedAtDesc(User user);

    List<ChatSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<ChatSession> findByIdAndUserId(Long id, Long userId);

    /**
     * Sessions for a user ordered by latest message time (falls back to session createdAt).
     */
    @Query("""
            SELECT s FROM ChatSession s
            LEFT JOIN s.messages m
            WHERE s.user.id = :userId
            GROUP BY s
            ORDER BY COALESCE(MAX(m.createdAt), s.createdAt) DESC
            """)
    List<ChatSession> findByUserIdOrderByLastActivityDesc(@Param("userId") Long userId);
}
