package com.ghInternship.GHInternship2026.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Модульные тесты для фасада {@link Message} в рамках Step 1.2.
 */
class MessageTest {

    @Test
    @DisplayName("Message.user should create ChatMessage with role 'user'")
    void shouldCreateUserMessageViaFacade() {
        ChatSession session = new ChatSession();
        ChatMessage msg = Message.user(session, "Привет от пользователя");

        assertThat(msg.getSession()).isEqualTo(session);
        assertThat(msg.getConversation()).isEqualTo(session);
        assertThat(msg.getContent()).isEqualTo("Привет от пользователя");
        assertThat(msg.getRole()).isEqualTo(ChatMessage.ROLE_USER);
        assertThat(msg.getSenderRole()).isEqualTo(ChatMessage.ROLE_USER);
        assertThat(msg.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Message.ai should create ChatMessage with role 'ai'")
    void shouldCreateAiMessageViaFacade() {
        ChatSession session = new ChatSession();
        ChatMessage msg = Message.ai(session, "Ответ искусственного интеллекта");

        assertThat(msg.getSession()).isEqualTo(session);
        assertThat(msg.getConversation()).isEqualTo(session);
        assertThat(msg.getContent()).isEqualTo("Ответ искусственного интеллекта");
        assertThat(msg.getRole()).isEqualTo(ChatMessage.ROLE_AI);
        assertThat(msg.getSenderRole()).isEqualTo(ChatMessage.ROLE_AI);
    }

    @Test
    @DisplayName("Message.of should support custom role and timestamp")
    void shouldCreateCustomMessageViaFacade() {
        ChatSession session = new ChatSession();
        LocalDateTime customTime = LocalDateTime.of(2026, 11, 24, 12, 0);
        ChatMessage msg = Message.of(session, "user", "Кастомное сообщение", customTime);

        assertThat(msg.getContent()).isEqualTo("Кастомное сообщение");
        assertThat(msg.getRole()).isEqualTo("user");
        assertThat(msg.getTimestamp()).isEqualTo(customTime);
    }
}
