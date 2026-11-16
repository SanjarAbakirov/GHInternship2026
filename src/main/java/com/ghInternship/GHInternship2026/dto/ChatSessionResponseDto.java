package com.ghInternship.GHInternship2026.dto;

import com.ghInternship.GHInternship2026.entity.ChatSession;

import java.time.LocalDateTime;

/**
 * DTO представления сессии чата пользователя для списков и истории диалогов.
 *
 * @param id уникальный идентификатор сессии
 * @param title заголовок (тема) диалога
 * @param createdAt дата и время создания сессии
 * @param updatedAt дата и время последнего обновления сессии
 * @param messageCount количество сохраненных сообщений в сессии
 * @param lastMessagePreview краткий фрагмент последнего сообщения для предпросмотра
 */
public record ChatSessionResponseDto(
        Long id,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        long messageCount,
        String lastMessagePreview
) {

    /**
     * Конструктор с базовыми полями для удобства и обратной совместимости.
     */
    public ChatSessionResponseDto(Long id, String title, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, title, createdAt, updatedAt, 0L, null);
    }

    /**
     * Алиас для удобного получения идентификатора сессии.
     */
    public Long sessionId() {
        return id;
    }

    /**
     * Фабричный метод создания DTO из JPA-сущности {@link ChatSession}.
     *
     * @param session JPA сущность сессии
     * @return сформированный объект DTO или {@code null}, если сущность отсутствует
     */
    public static ChatSessionResponseDto fromEntity(ChatSession session) {
        if (session == null) {
            return null;
        }
        return new ChatSessionResponseDto(
                session.getId(),
                session.getTitle(),
                session.getCreatedAt(),
                session.getUpdatedAt(),
                0L,
                null
        );
    }

    /**
     * Фабричный метод создания DTO из JPA-сущности {@link ChatSession} с количеством сообщений и превью.
     *
     * @param session JPA сущность сессии
     * @param messageCount количество сообщений
     * @param lastMessagePreview текст последнего сообщения
     * @return сформированный объект DTO
     */
    public static ChatSessionResponseDto fromEntity(ChatSession session, long messageCount, String lastMessagePreview) {
        if (session == null) {
            return null;
        }
        return new ChatSessionResponseDto(
                session.getId(),
                session.getTitle(),
                session.getCreatedAt(),
                session.getUpdatedAt(),
                messageCount,
                lastMessagePreview
        );
    }
}

