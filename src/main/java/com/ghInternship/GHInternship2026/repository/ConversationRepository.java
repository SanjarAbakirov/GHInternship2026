package com.ghInternship.GHInternship2026.repository;

import com.ghInternship.GHInternship2026.entity.ChatSession;
import com.ghInternship.GHInternship2026.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA репозиторий для управления диалогами (ConversationRepository).
 *
 * <p>Соответствует спецификации плана персистентности чата (Phase 2):
 * <ul>
 *   <li>Расширяет интерфейс {@link JpaRepository} для предоставления CRUD-операций над беседами</li>
 *   <li>Предоставляет кастомный запрос для выборки диалогов по ID пользователя с упорядочиванием
 *       по времени обновления ({@link #findByUserIdOrderByUpdatedAtDesc(Long)})</li>
 *   <li>Предоставляет кастомный запрос для поиска диалога по ID и ID пользователя для валидации
 *       прав владения и изоляции данных ({@link #findByIdAndUserId(Long, Long)})</li>
 * </ul>
 */
@Repository
public interface ConversationRepository extends JpaRepository<ChatSession, Long> {

    /**
     * Найти все диалоги (conversations) пользователя по его ID, упорядоченные по времени последнего обновления
     * (update time) в порядке убывания (сначала самые недавние).
     *
     * @param userId уникальный идентификатор пользователя-владельца
     * @return список диалогов пользователя, отсортированных по убыванию времени обновления
     */
    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(Long userId);

    /**
     * Найти все диалоги по объекту пользователя {@link User}, упорядоченные по времени последнего обновления.
     *
     * @param user пользователь-владелец
     * @return список диалогов
     */
    List<ChatSession> findByUserOrderByUpdatedAtDesc(User user);

    /**
     * Найти конкретный диалог по его ID и ID пользователя-владельца.
     * Используется для валидации прав владения (ownership validation) и предотвращения атак класса IDOR.
     *
     * @param id ID диалога (conversation ID)
     * @param userId ID пользователя-владельца
     * @return {@link Optional} с найденным диалогом, либо пустой Optional, если диалог не найден или принадлежит другому пользователю
     */
    Optional<ChatSession> findByIdAndUserId(Long id, Long userId);

    /**
     * Найти конкретный диалог по его ID и сущности владельца {@link User}.
     *
     * @param id ID диалога
     * @param user пользователь-владелец
     * @return {@link Optional} с найденным диалогом
     */
    Optional<ChatSession> findByIdAndUser(Long id, User user);

    /**
     * Проверить принадлежность диалога конкретному пользователю по ID (быстрая проверка ownership validation).
     *
     * @param id ID диалога
     * @param userId ID пользователя
     * @return {@code true}, если диалог существует и принадлежит указанному пользователю
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * Найти диалог с жадной загрузкой (JOIN FETCH) связанного пользователя для исключения LazyInitializationException.
     *
     * @param id ID диалога
     * @return {@link Optional} с загруженным диалогом и пользователем
     */
    @Query("SELECT s FROM ChatSession s JOIN FETCH s.user WHERE s.id = :id")
    Optional<ChatSession> findByIdWithUser(@Param("id") Long id);
}
