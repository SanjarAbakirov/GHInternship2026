package com.ghInternship.GHInternship2026.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA сущность для отдельного сообщения в рамках сессии диалога {@link ChatSession}.
 *
 * <p>Хранит текст сообщения (поддерживает длинные ответы AI формата TEXT),
 * временную метку отправки и роль отправителя ({@code "user"} или {@code "ai"}).
 * Связана отношением многие-к-одному ({@link ManyToOne}) с родительской сессией диалога.
 */
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_messages_session_id", columnList = "session_id"),
        @Index(name = "idx_chat_messages_timestamp", columnList = "timestamp")
})
public class ChatMessage {

    public static final String ROLE_USER = "user";
    public static final String ROLE_AI = "ai";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    /**
     * Текст сообщения (типа TEXT в PostgreSQL и H2 для поддержки больших ответов нейросети).
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Роль отправителя: {@code "user"} (пользователь) или {@code "ai"} (нейросеть / ассистент).
     */
    @Column(name = "sender_role", nullable = false, length = 20)
    private String senderRole;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    /**
     * Конструктор без параметров, обязательный по спецификации JPA.
     */
    public ChatMessage() {
    }

    /**
     * Конструктор для создания сообщения с автоматической временной меткой.
     *
     * @param session родительская сессия диалога
     * @param senderRole роль отправителя ("user" или "ai")
     * @param content текст сообщения
     */
    public ChatMessage(ChatSession session, String senderRole, String content) {
        this(session, senderRole, content, LocalDateTime.now());
    }

    /**
     * Конструктор с явным указанием временной метки.
     */
    public ChatMessage(ChatSession session, String senderRole, String content, LocalDateTime timestamp) {
        this.session = session;
        this.senderRole = normalizeRole(senderRole);
        this.content = content;
        this.timestamp = (timestamp != null) ? timestamp : LocalDateTime.now();
    }

    /**
     * Полный конструктор со всеми полями.
     */
    public ChatMessage(Long id, ChatSession session, String senderRole, String content, LocalDateTime timestamp) {
        this.id = id;
        this.session = session;
        this.senderRole = normalizeRole(senderRole);
        this.content = content;
        this.timestamp = (timestamp != null) ? timestamp : LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
        if (this.senderRole == null || this.senderRole.isBlank()) {
            this.senderRole = ROLE_USER;
        }
    }

    // --- Фабричные методы ---

    /**
     * Создать сообщение пользователя ("user").
     */
    public static ChatMessage userMessage(ChatSession session, String content) {
        return new ChatMessage(session, ROLE_USER, content);
    }

    /**
     * Создать сообщение искусственного интеллекта ("ai").
     */
    public static ChatMessage aiMessage(ChatSession session, String content) {
        return new ChatMessage(session, ROLE_AI, content);
    }

    private static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return ROLE_USER;
        }
        String trimmed = role.trim().toLowerCase();
        if ("assistant".equals(trimmed)) {
            return ROLE_AI;
        }
        return trimmed;
    }

    // --- Геттеры и сеттеры (включая алиасы ТЗ) ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChatSession getSession() {
        return session;
    }

    public void setSession(ChatSession session) {
        this.session = session;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Алиас для {@link #getContent()} в соответствии с ТЗ (message content).
     */
    public String getMessageContent() {
        return content;
    }

    /**
     * Алиас для {@link #setContent(String)}.
     */
    public void setMessageContent(String messageContent) {
        this.content = messageContent;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = normalizeRole(senderRole);
    }

    /**
     * Алиас для {@link #getSenderRole()} в соответствии с ТЗ (role of sender).
     */
    public String getSender() {
        return senderRole;
    }

    /**
     * Алиас для {@link #setSenderRole(String)}.
     */
    public void setSender(String sender) {
        this.senderRole = normalizeRole(sender);
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // --- equals, hashCode и toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMessage that = (ChatMessage) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "id=" + id +
                ", senderRole='" + senderRole + '\'' +
                ", timestamp=" + timestamp +
                ", contentLength=" + (content != null ? content.length() : 0) +
                '}';
    }
}

