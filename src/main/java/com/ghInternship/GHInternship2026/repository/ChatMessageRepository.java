package com.ghInternship.GHInternship2026.repository;

import com.ghInternship.GHInternship2026.entity.ChatMessage;
import com.ghInternship.GHInternship2026.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA репозиторий для управления сущностями сообщений чата {@link ChatMessage}.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Получить хронологический список сообщений для указанной сессии диалога.
     *
     * @param session сессия диалога
     * @return упорядоченный по возрастанию времени список сообщений
     */
    List<ChatMessage> findBySessionOrderByTimestampAsc(ChatSession session);

    /**
     * Получить хронологический список сообщений по идентификатору сессии.
     *
     * @param sessionId ID сессии диалога
     * @return упорядоченный по возрастанию времени список сообщений
     */
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(Long sessionId);

    /**
     * Подсчитать количество сообщений в указанной сессии.
     *
     * @param session сессия диалога
     * @return количество сообщений
     */
    long countBySession(ChatSession session);
}

