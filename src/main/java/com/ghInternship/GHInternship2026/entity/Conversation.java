package com.ghInternship.GHInternship2026.entity;

/**
 * Семантический класс-синоним и фабричный фасад для сущности {@link ChatSession}.
 *
 * <p>Соответствует спецификации плана персистентности:
 * <b>Phase 1: Create JPA Entities (Models) &mdash; Step 1.1: Create Conversation Entity</b>.
 *
 * <p>В архитектуре проекта GHInternship2026 для представления беседы (conversation / chat session)
 * используется производственная JPA-сущность {@link ChatSession}, которая реализует все требования
 * спецификации:
 * <ul>
 *   <li><b>Unique ID:</b> уникальный автогенерируемый идентификатор {@code id} (IDENTITY)</li>
 *   <li><b>Title:</b> тема сессии {@code title} с автоматической генерацией по первому сообщению</li>
 *   <li><b>Model Name:</b> имя языковой модели {@code modelName} (по умолчанию {@value ChatSession#DEFAULT_MODEL})</li>
 *   <li><b>Timestamps:</b> временные метки создания {@code createdAt} и обновления {@code updatedAt}</li>
 *   <li><b>Relationship to User:</b> отношение {@link jakarta.persistence.ManyToOne} к сущности {@link User}</li>
 *   <li><b>Relationship to Messages:</b> двустороннее отношение {@link jakarta.persistence.OneToMany} к {@link ChatMessage}</li>
 *   <li><b>Cascade settings:</b> каскадное сохранение/удаление {@code CascadeType.ALL} и {@code orphanRemoval = true}</li>
 * </ul>
 *
 * @see ChatSession
 * @see ChatMessage
 */
public final class Conversation {

    private Conversation() {
        // Фабричный класс-фасад для семантического соответствия спецификации Step 1.1
    }

    /**
     * Создать новую сущность диалога (Conversation / ChatSession) с указанием пользователя, заголовка и имени модели.
     *
     * @param user владелец сессии диалога
     * @param title заголовок темы диалога
     * @param modelName имя используемой AI-модели
     * @return инициализированная сущность {@link ChatSession}
     */
    public static ChatSession of(User user, String title, String modelName) {
        return new ChatSession(user, title, modelName);
    }

    /**
     * Создать новую сущность диалога с дефолтной языковой моделью.
     *
     * @param user владелец сессии диалога
     * @param title заголовок темы диалога
     * @return инициализированная сущность {@link ChatSession}
     */
    public static ChatSession of(User user, String title) {
        return new ChatSession(user, title);
    }

    /**
     * Создать новую сущность диалога с дефолтным заголовком и моделью.
     *
     * @param user владелец сессии диалога
     * @return инициализированная сущность {@link ChatSession}
     */
    public static ChatSession of(User user) {
        return new ChatSession(user);
    }
}

