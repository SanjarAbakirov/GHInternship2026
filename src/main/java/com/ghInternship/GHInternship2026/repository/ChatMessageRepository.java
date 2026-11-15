package com.ghInternship.GHInternship2026.repository;

import com.ghInternship.GHInternship2026.entity.ChatMessage;
import com.ghInternship.GHInternship2026.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA репозиторий для управления сущностями сообщений чата {@link ChatMessage}.
 *
 * <p>Предоставляет полный набор стандартных CRUD-операций ({@code save}, {@code findById},
 * {@code findAll}, {@code delete}, {@code count}), а также кастомные методы выборки
 * сообщений конкретной сессии в прямом и обратном хронологическом порядке.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Получить хронологический список сообщений для указанной сессии диалога (старые $\rightarrow$ новые).
     *
     * @param session сессия диалога
     * @return упорядоченный по возрастанию времени список сообщений
     */
    List<ChatMessage> findBySessionOrderByTimestampAsc(ChatSession session);

    /**
     * Получить хронологический список сообщений по идентификатору сессии (старые $\rightarrow$ новые).
     *
     * @param sessionId ID сессии диалога
     * @return упорядоченный по возрастанию времени список сообщений
     */
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(Long sessionId);

    /**
     * Получить список сообщений сессии в обратном хронологическом порядке (сначала новые).
     *
     * @param session сессия диалога
     * @return упорядоченный по убыванию времени список сообщений
     */
    List<ChatMessage> findBySessionOrderByTimestampDesc(ChatSession session);

    /**
     * Получить список сообщений по ID сессии в обратном хронологическом порядке (сначала новые).
     *
     * @param sessionId ID сессии диалога
     * @return упорядоченный по убыванию времени список сообщений
     */
    List<ChatMessage> findBySessionIdOrderByTimestampDesc(Long sessionId);

    /**
     * Получить самое последнее сообщение в сессии (наиболее свежая активность/превью).
     *
     * @param session сессия диалога
     * @return {@link Optional} с последним сообщением
     */
    Optional<ChatMessage> findFirstBySessionOrderByTimestampDesc(ChatSession session);

    /**
     * Получить самое последнее сообщение по ID сессии.
     *
     * @param sessionId ID сессии диалога
     * @return {@link Optional} с последним сообщением
     */
    Optional<ChatMessage> findFirstBySessionIdOrderByTimestampDesc(Long sessionId);

    /**
     * Подсчитать количество сообщений в указанной сессии.
     *
     * @param session сессия диалога
     * @return количество сообщений
     */
    long countBySession(ChatSession session);

    /**
     * Подсчитать количество сообщений по ID сессии.
     *
     * @param sessionId ID сессии
     * @return количество сообщений
     */
    long countBySessionId(Long sessionId);

    /**
     * Удалить все сообщения указанной сессии.
     *
     * @param session сессия диалога
     */
    @Modifying
    void deleteBySession(ChatSession session);

    /**
     * Удалить все сообщения по идентификатору сессии.
     *
     * @param sessionId ID сессии диалога
     */
    @Modifying
    void deleteBySessionId(Long sessionId);
}
