package com.ghInternship.GHInternship2026.dto;

import com.ghInternship.GHInternship2026.entity.ChatMessage;

import java.time.LocalDateTime;

/**
 * DTO отдельного сообщения чата в истории сессии диалога.
 *
 * @param id уникальный идентификатор сообщения
 * @param sessionId идентификатор связанной сессии чата
 * @param senderRole роль отправителя ("user" или "ai")
 * @param content текстовое содержимое сообщения
 * @param timestamp дата и время отправки сообщения
 */
public record ChatMessageResponseDto(
        Long id,
        Long sessionId,
        String senderRole,
        String content,
        LocalDateTime timestamp
) {

    /**
     * Конструктор без явного sessionId для гибкости и обратной совместимости.
     */
    public ChatMessageResponseDto(Long id, String senderRole, String content, LocalDateTime timestamp) {
        this(id, null, senderRole, content, timestamp);
    }

    /**
     * Алиас роли отправителя.
     */
    public String role() {
        return senderRole;
    }

    /**
     * Алиас отправителя.
     */
    public String sender() {
        return senderRole;
    }

    /**
     * Алиас содержимого сообщения.
     */
    public String message() {
        return content;
    }

    /**
     * Алиас текстового сообщения.
     */
    public String text() {
        return content;
    }

    /**
     * Фабричный метод создания DTO из JPA-сущности {@link ChatMessage}.
     *
     * @param message JPA сущность сообщения
     * @return сформированный объект DTO или {@code null}, если сущность отсутствует
     */
    public static ChatMessageResponseDto fromEntity(ChatMessage message) {
        if (message == null) {
            return null;
        }
        Long sId = (message.getSession() != null) ? message.getSession().getId() : null;
        return new ChatMessageResponseDto(
                message.getId(),
                sId,
                message.getSenderRole(),
                message.getContent(),
                message.getTimestamp()
        );
    }
}

