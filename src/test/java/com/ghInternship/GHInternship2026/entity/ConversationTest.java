package com.ghInternship.GHInternship2026.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Модульные тесты для фасада {@link Conversation} в рамках Step 1.1.
 */
class ConversationTest {

    @Test
    @DisplayName("Conversation.of(user) should produce ChatSession with defaults")
    void shouldCreateConversationWithDefaults() {
        User user = new User("alice", "hash");
        ChatSession session = Conversation.of(user);

        assertThat(session.getUser()).isEqualTo(user);
        assertThat(session.getTitle()).isEqualTo(ChatSession.DEFAULT_TITLE);
        assertThat(session.getModelName()).isEqualTo(ChatSession.DEFAULT_MODEL);
        assertThat(session.getCreatedAt()).isNotNull();
        assertThat(session.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Conversation.of(user, title) should produce ChatSession with custom title")
    void shouldCreateConversationWithCustomTitle() {
        User user = new User("bob", "hash");
        ChatSession session = Conversation.of(user, "Архитектура Spring");

        assertThat(session.getUser()).isEqualTo(user);
        assertThat(session.getTitle()).isEqualTo("Архитектура Spring");
        assertThat(session.getModelName()).isEqualTo(ChatSession.DEFAULT_MODEL);
    }

    @Test
    @DisplayName("Conversation.of(user, title, modelName) should set custom model name")
    void shouldCreateConversationWithCustomModel() {
        User user = new User("carol", "hash");
        ChatSession session = Conversation.of(user, "Анализ данных", "gpt-4o");

        assertThat(session.getUser()).isEqualTo(user);
        assertThat(session.getTitle()).isEqualTo("Анализ данных");
        assertThat(session.getModelName()).isEqualTo("gpt-4o");
    }
}

