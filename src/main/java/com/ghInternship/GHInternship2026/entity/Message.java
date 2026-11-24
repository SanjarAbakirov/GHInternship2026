package com.ghInternship.GHInternship2026.entity;

import java.time.LocalDateTime;

/**
 * Семантический класс-фасад для сущности {@link ChatMessage}.
 *
 * <p>Соответствует спецификации плана персистентности:
 * <b>Phase 1: Create JPA Entities (Models) &mdash; Step 1.2: Create Message Entity</b>.
 *
 * <p>В архитектуре проекта GHInternship2026 для представления отдельного сообщения
 * диалога используется производственная JPA-сущность {@link ChatMessage}, которая полностью
 * реализует все требования спецификации:
 * <ul>
 *   <li><b>Unique ID:</b> уникальный автогенерируемый идентификатор {@code id} (IDENTITY)</li>
 *   <li><b>Content:</b> текст сообщения с поддержкой длинных ответов формата {@code TEXT}</li>
 *   <li><b>Role (user/AI):</b> роль отправителя {@code senderRole} (алиас {@code role}: "user" или "ai")</li>
 *   <li><b>Timestamp:</b> временная метка отправки {@code timestamp} (LocalDateTime)</li>
 *   <li><b>Relationship to Conversation:</b> отношение {@link jakarta.persistence.ManyToOne} к сессии диалога {@link ChatSession}</li>
 * </ul>
 *
 * @see ChatMessage
 * @see ChatSession
 * @see Conversation
 */
public final class Message {

    private Message() {
        // Фасадный класс со статическими фабричными методами
    }

    /**
     * Создать сообщение пользователя ("user").
     */
    public static ChatMessage user(ChatSession conversation, String content) {
        return ChatMessage.userMessage(conversation, content);
    }

    /**
     * Создать сообщение AI-ассистента ("ai").
     */
    public static ChatMessage ai(ChatSession conversation, String content) {
        return ChatMessage.aiMessage(conversation, content);
    }

    /**
     * Создать сообщение с произвольной ролью.
     */
    public static ChatMessage of(ChatSession conversation, String role, String content) {
        return new ChatMessage(conversation, role, content);
    }

    /**
     * Создать сообщение с явным указанием времени.
     */
    public static ChatMessage of(ChatSession conversation, String role, String content, LocalDateTime timestamp) {
        return new ChatMessage(conversation, role, content, timestamp);
    }
}
