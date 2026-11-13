package com.ghInternship.GHInternship2026.repository;

import com.ghInternship.GHInternship2026.entity.ChatSession;
import com.ghInternship.GHInternship2026.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA репозиторий для управления сущностями сессий чата {@link ChatSession}.
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    /**
     * Найти все сессии диалогов указанного пользователя, упорядоченные от новых к старым.
     *
     * @param user пользователь-владелец сессий
     * @return список сессий диалогов
     */
    List<ChatSession> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Найти все сессии диалогов по идентификатору пользователя.
     *
     * @param userId ID пользователя
     * @return список сессий диалогов
     */
    List<ChatSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Найти конкретную сессию по ее ID и владельцу (обеспечивает безопасность и изоляцию данных).
     *
     * @param id ID сессии
     * @param user пользователь-владелец
     * @return {@link Optional} с найденной сессией
     */
    Optional<ChatSession> findByIdAndUser(Long id, User user);

    /**
     * Найти конкретную сессию по ее ID и ID владельца.
     *
     * @param id ID сессии
     * @param userId ID пользователя-владельца
     * @return {@link Optional} с найденной сессией
     */
    Optional<ChatSession> findByIdAndUserId(Long id, Long userId);
}

