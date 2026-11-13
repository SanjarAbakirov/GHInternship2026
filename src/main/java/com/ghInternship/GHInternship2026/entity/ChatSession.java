package com.ghInternship.GHInternship2026.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JPA сущность для отдельной сессии диалога (чата) пользователя с AI.
 *
 * <p>Связана отношением многие-к-одному ({@link ManyToOne}) с сущностью {@link User}
 * и один-ко-многим ({@link OneToMany}) с сообщениями {@link ChatMessage}.
 * Каскадные операции ({@link CascadeType#ALL}) и {@code orphanRemoval = true}
 * гарантируют корректное сохранение и каскадное удаление сообщений при удалении сессии.
 */
@Entity
@Table(name = "chat_sessions", indexes = {
        @Index(name = "idx_chat_sessions_user_id", columnList = "user_id"),
        @Index(name = "idx_chat_sessions_created_at", columnList = "created_at")
})
public class ChatSession {

    public static final String DEFAULT_TITLE = "Новый диалог";
    public static final int MAX_TITLE_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp ASC")
    private List<ChatMessage> messages = new ArrayList<>();

    /**
     * Конструктор без параметров, обязательный по спецификации JPA.
     */
    public ChatSession() {
    }

    /**
     * Конструктор для создания сессии с привязкой к пользователю и автоматическим заголовком.
     *
     * @param user владелец сессии диалога
     */
    public ChatSession(User user) {
        this(user, DEFAULT_TITLE);
    }

    /**
     * Конструктор для создания сессии с указанием заголовка.
     *
     * @param user владелец сессии диалога
     * @param title заголовок темы диалога
     */
    public ChatSession(User user, String title) {
        this.user = user;
        this.title = (title != null && !title.isBlank()) ? title.trim() : DEFAULT_TITLE;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Полный конструктор со всеми полями.
     */
    public ChatSession(Long id, User user, String title, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.title = (title != null && !title.isBlank()) ? title.trim() : DEFAULT_TITLE;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.title == null || this.title.isBlank()) {
            this.title = DEFAULT_TITLE;
        }
    }

    // --- Управление связью с сообщениями (Bidirectional Helpers) ---

    /**
     * Добавить сообщение в сессию с синхронизацией обратной ссылки.
     *
     * @param message сообщение для добавления
     */
    public void addMessage(ChatMessage message) {
        if (message != null) {
            this.messages.add(message);
            message.setSession(this);
        }
    }

    /**
     * Удалить сообщение из сессии с обнулением обратной ссылки.
     *
     * @param message сообщение для удаления
     */
    public void removeMessage(ChatMessage message) {
        if (message != null) {
            this.messages.remove(message);
            message.setSession(null);
        }
    }

    /**
     * Утилитный метод для генерации заголовка сессии на основе первого сообщения пользователя.
     *
     * @param firstMessageContent текст первого сообщения
     * @return краткий заголовок (не более 50 символов)
     */
    public static String generateTitleFromMessage(String firstMessageContent) {
        if (firstMessageContent == null || firstMessageContent.isBlank()) {
            return DEFAULT_TITLE;
        }
        String clean = firstMessageContent.replaceAll("\\s+", " ").trim();
        int maxLength = 50;
        if (clean.length() <= maxLength) {
            return clean;
        }
        return clean.substring(0, maxLength).trim() + "...";
    }

    // --- Геттеры и сеттеры ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = (title != null && !title.isBlank()) ? title.trim() : DEFAULT_TITLE;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Алиас для {@link #getCreatedAt()} в соответствии с требованиями ТЗ (timestamp).
     */
    public LocalDateTime getTimestamp() {
        return createdAt;
    }

    /**
     * Алиас для {@link #setCreatedAt(LocalDateTime)} в соответствии с требованиями ТЗ.
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.createdAt = timestamp;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = (messages != null) ? messages : new ArrayList<>();
    }

    // --- equals, hashCode и toString ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatSession that = (ChatSession) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ChatSession{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", createdAt=" + createdAt +
                ", messagesCount=" + messages.size() +
                '}';
    }
}

