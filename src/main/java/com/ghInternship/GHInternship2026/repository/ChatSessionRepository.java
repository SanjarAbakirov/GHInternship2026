package com.ghInternship.GHInternship2026.repository;

import com.ghInternship.GHInternship2026.entity.ChatSession;
import com.ghInternship.GHInternship2026.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA репозиторий для управления сущностями сессий чата {@link ChatSession}.
 *
 * <p>Предоставляет полный набор стандартных CRUD-операций ({@code save}, {@code findById},
 * {@code findAll}, {@code delete}, {@code count}), а также кастомные методы выборки
 * сессий конкретного пользователя с упорядочиванием по наиболее недавней активности.
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    /**
     * Найти все сессии диалогов указанного пользователя, упорядоченные по времени создания (сначала новые).
     *
     * @param user пользователь-владелец сессий
     * @return список сессий диалогов
     */
    List<ChatSession> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Найти все сессии диалогов по идентификатору пользователя, упорядоченные по времени создания.
     *
     * @param userId ID пользователя
     * @return список сессий диалогов
     */
    List<ChatSession> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Найти все сессии пользователя, упорядоченные по времени последнего обновления (наиболее недавняя активность).
     *
     * @param user пользователь-владелец
     * @return список сессий диалогов
     */
    List<ChatSession> findByUserOrderByUpdatedAtDesc(User user);

    /**
     * Найти все сессии по ID пользователя, упорядоченные по времени последнего обновления.
     *
     * @param userId ID пользователя
     * @return список сессий диалогов
     */
    List<ChatSession> findByUserIdOrderByUpdatedAtDesc(Long userId);

    /**
     * Кастомный JPQL-запрос: найти все сессии пользователя, упорядоченные по времени последнего сообщения
     * (динамический расчет по max(timestamp) сообщений сессии с фоллбэком на updatedAt/createdAt).
     *
     * @param user пользователь-владелец
     * @return список сессий, упорядоченный по недавней активности сообщений
     */
    @Query("SELECT s FROM ChatSession s " +
           "LEFT JOIN s.messages m " +
           "WHERE s.user = :user " +
           "GROUP BY s.id, s.title, s.createdAt, s.updatedAt, s.user " +
           "ORDER BY COALESCE(MAX(m.timestamp), s.updatedAt, s.createdAt) DESC")
    List<ChatSession> findByUserOrderByMostRecentActivityDesc(@Param("user") User user);

    /**
     * Кастомный JPQL-запрос по ID пользователя: упорядочивание по недавней активности сообщений.
     *
     * @param userId ID пользователя
     * @return список сессий, упорядоченный по недавней активности сообщений
     */
    @Query("SELECT s FROM ChatSession s " +
           "LEFT JOIN s.messages m " +
           "WHERE s.user.id = :userId " +
           "GROUP BY s.id, s.title, s.createdAt, s.updatedAt, s.user " +
           "ORDER BY COALESCE(MAX(m.timestamp), s.updatedAt, s.createdAt) DESC")
    List<ChatSession> findByUserIdOrderByMostRecentActivityDesc(@Param("userId") Long userId);

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

    /**
     * Проверить принадлежность сессии конкретному пользователю.
     *
     * @param id ID сессии
     * @param user пользователь
     * @return {@code true}, если сессия принадлежит пользователю
     */
    boolean existsByIdAndUser(Long id, User user);

    /**
     * Проверить принадлежность сессии пользователю по его ID.
     *
     * @param id ID сессии
     * @param userId ID пользователя
     * @return {@code true}, если сессия принадлежит пользователю
     */
    boolean existsByIdAndUserId(Long id, Long userId);

    /**
     * Подсчитать количество сессий у пользователя.
     *
     * @param user пользователь
     * @return количество сессий
     */
    long countByUser(User user);

    /**
     * Подсчитать количество сессий по ID пользователя.
     *
     * @param userId ID пользователя
     * @return количество сессий
     */
    long countByUserId(Long userId);

    /**
     * Найти сессию по идентификатору с немедленной (EAGER) загрузкой связанного пользователя (JOIN FETCH).
     *
     * @param id идентификатор сессии
     * @return сессия с инициализированным пользователем
     */
    @Query("SELECT s FROM ChatSession s JOIN FETCH s.user WHERE s.id = :id")
    Optional<ChatSession> findByIdWithUser(@Param("id") Long id);

    /**
     * Безопасно удалить сессию по ID только если она принадлежит указанному пользователю.
     *
     * @param id ID сессии
     * @param user пользователь-владелец
     */
    @Modifying
    void deleteByIdAndUser(Long id, User user);

    /**
     * Безопасно удалить сессию по ID и ID пользователя.
     *
     * @param id ID сессии
     * @param userId ID пользователя
     */
    @Modifying
    void deleteByIdAndUserId(Long id, Long userId);
}
