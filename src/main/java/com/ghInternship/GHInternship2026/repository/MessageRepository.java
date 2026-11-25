package com.ghInternship.GHInternship2026.repository;

import com.ghInternship.GHInternship2026.entity.ChatMessage;
import com.ghInternship.GHInternship2026.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA репозиторий для управления сообщениями (MessageRepository).
 *
 * <p>Соответствует спецификации плана персистентности чата (Phase 2 &mdash; Step 2.2):
 * <ul>
 *   <li>Create repository interface extending {@link JpaRepository}</li>
 *   <li>Add custom query method to find messages by conversation ID, ordered chronologically ({@link #findBySessionIdOrderByTimestampAsc(Long)})</li>
 *   <li>Add custom delete method for clearing conversation history ({@link #deleteBySessionId(Long)}, {@link #clearConversationHistory(Long)})</li>
 * </ul>
 */
@Repository
public interface MessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Найти все сообщения по ID диалога (conversation / session ID), упорядоченные в хронологическом порядке (ASC).
     *
     * @param conversationId уникальный идентификатор беседы
     * @return хронологический список сообщений (от старых к новым)
     */
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(Long conversationId);

    /**
     * Найти все сообщения беседы по объекту {@link ChatSession}, упорядоченные в хронологическом порядке.
     *
     * @param conversation сущность беседы
     * @return хронологический список сообщений
     */
    List<ChatMessage> findBySessionOrderByTimestampAsc(ChatSession conversation);

    /**
     * Кастомный JPQL-запрос для выборки сообщений по ID беседы с явным хронологическим упорядочиванием.
     *
     * @param conversationId ID беседы
     * @return хронологический список сообщений
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.session.id = :conversationId ORDER BY m.timestamp ASC")
    List<ChatMessage> findByConversationId(@Param("conversationId") Long conversationId);

    /**
     * Найти сообщения беседы в обратном хронологическом порядке (DESC, сначала свежие).
     *
     * @param conversationId ID беседы
     * @return список сообщений от самых свежих к старым
     */
    List<ChatMessage> findBySessionIdOrderByTimestampDesc(Long conversationId);

    /**
     * Найти самое последнее сообщение в беседе (для генерации превью ленты).
     *
     * @param conversationId ID беседы
     * @return {@link Optional} с последним сообщением
     */
    Optional<ChatMessage> findFirstBySessionIdOrderByTimestampDesc(Long conversationId);

    /**
     * Подсчитать количество сообщений в указанной беседе.
     *
     * @param conversationId ID беседы
     * @return число сообщений
     */
    long countBySessionId(Long conversationId);

    /**
     * Кастомный метод удаления для очистки истории диалога по ID беседы.
     *
     * @param conversationId ID беседы, сообщения которой необходимо удалить
     */
    @Modifying
    @Query("DELETE FROM ChatMessage m WHERE m.session.id = :conversationId")
    void clearConversationHistory(@Param("conversationId") Long conversationId);

    /**
     * Удалить все сообщения беседы по ее идентификатору (Spring Data derived delete).
     *
     * @param conversationId ID беседы
     */
    @Modifying
    void deleteBySessionId(Long conversationId);

    /**
     * Удалить все сообщения беседы по объекту {@link ChatSession}.
     *
     * @param conversation сущность беседы
     */
    @Modifying
    void deleteBySession(ChatSession conversation);
}
