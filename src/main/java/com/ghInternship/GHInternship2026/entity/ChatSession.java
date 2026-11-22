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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JPA сущность для отдельной сессии диалога (чата / conversation) пользователя с AI.
 *
 * <p>Реализует концепцию Conversation Entity (Phase 1, Step 1.1) для долговременного хранения
 * истории диалогов в базе данных PostgreSQL.
 *
 * <p>Связана отношением многие-к-одному ({@link ManyToOne}) с сущностью {@link User}
 * и один-ко-многим ({@link OneToMany}) с сообщениями {@link ChatMessage}.
 * Каскадные операции ({@link CascadeType#ALL}) и {@code orphanRemoval = true}
 * гарантируют корректное сохранение и каскадное удаление сообщений при удалении сессии.
 */
@Entity
@Table(name = "chat_sessions", indexes = {
        @Index(name = "idx_chat_sessions_user_id", columnList = "user_id"),
        @Index(name = "idx_chat_sessions_created_at", columnList = "created_at"),
        @Index(name = "idx_chat_sessions_updated_at", columnList = "updated_at")
})
public class ChatSession {

    public static final String DEFAULT_TITLE = "Новый диалог";
    public static final String DEFAULT_MODEL = "gpt-3.5-turbo";
    public static final int MAX_TITLE_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    private String title;

    /**
     * Имя используемой языковой модели AI (например, "gpt-3.5-turbo", "gpt-4o", "gemini-1.5-pro").
     */
    @Column(name = "model_name", length = 50)
    private String modelName = DEFAULT_MODEL;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Время последней активности (отправки сообщения или обновления сессии).
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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
        this(user, title, DEFAULT_MODEL);
    }

    /**
     * Конструктор для создания сессии с указанием заголовка и имени используемой модели.
     *
     * @param user владелец сессии диалога
     * @param title заголовок темы диалога
     * @param modelName имя используемой языковой модели
     */
    public ChatSession(User user, String title, String modelName) {
        this.user = user;
        this.title = (title != null && !title.isBlank()) ? title.trim() : DEFAULT_TITLE;
        this.modelName = (modelName != null && !modelName.isBlank()) ? modelName.trim() : DEFAULT_MODEL;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /**
     * Конструктор со всеми базовыми полями.
     */
    public ChatSession(Long id, User user, String title, LocalDateTime createdAt) {
        this(id, user, title, DEFAULT_MODEL, createdAt, createdAt);
    }

    /**
     * Полный конструктор со всеми полями и временем последней активности.
     */
    public ChatSession(Long id, User user, String title, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, user, title, DEFAULT_MODEL, createdAt, updatedAt);
    }

    /**
     * Полный конструктор с явным указанием имени модели AI.
     */
    public ChatSession(Long id, User user, String title, String modelName, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.title = (title != null && !title.isBlank()) ? title.trim() : DEFAULT_TITLE;
        this.modelName = (modelName != null && !modelName.isBlank()) ? modelName.trim() : DEFAULT_MODEL;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
        this.updatedAt = (updatedAt != null) ? updatedAt : this.createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = this.createdAt;
        }
        if (this.title == null || this.title.isBlank()) {
            this.title = DEFAULT_TITLE;
        }
        if (this.modelName == null || this.modelName.isBlank()) {
            this.modelName = DEFAULT_MODEL;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Управление связью с сообщениями (Bidirectional Helpers) ---

    /**
     * Добавить сообщение в сессию с синхронизацией обратной ссылки и обновлением времени активности.
     *
     * @param message сообщение для добавления
     */
    public void addMessage(ChatMessage message) {
        if (message != null) {
            this.messages.add(message);
            message.setSession(this);
            this.updatedAt = (message.getTimestamp() != null) ? message.getTimestamp() : LocalDateTime.now();
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

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = (modelName != null && !modelName.isBlank()) ? modelName.trim() : DEFAULT_MODEL;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Алиас для {@link #getUpdatedAt()} — время наиболее недавней активности в диалоге.
     */
    public LocalDateTime getLastActivityAt() {
        return updatedAt;
    }

    /**
     * Алиас для {@link #setUpdatedAt(LocalDateTime)}.
     */
    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.updatedAt = lastActivityAt;
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
                ", modelName='" + modelName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", messagesCount=" + messages.size() +
                '}';
    }
}

