package com.ghInternship.GHInternship2026.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Модульные тесты для JPA-сущности {@link ChatMessage}.
 */
class ChatMessageTest {

    @Test
    @DisplayName("Should create user message with factory method")
    void shouldCreateUserMessageViaFactory() {
        ChatSession session = new ChatSession();
        ChatMessage message = ChatMessage.userMessage(session, "Привет, AI!");

        assertThat(message.getSession()).isEqualTo(session);
        assertThat(message.getContent()).isEqualTo("Привет, AI!");
        assertThat(message.getMessageContent()).isEqualTo("Привет, AI!");
        assertThat(message.getSenderRole()).isEqualTo(ChatMessage.ROLE_USER);
        assertThat(message.getSender()).isEqualTo(ChatMessage.ROLE_USER);
        assertThat(message.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should create AI message with factory method")
    void shouldCreateAiMessageViaFactory() {
        ChatSession session = new ChatSession();
        ChatMessage message = ChatMessage.aiMessage(session, "Привет! Чем могу помочь?");

        assertThat(message.getSession()).isEqualTo(session);
        assertThat(message.getContent()).isEqualTo("Привет! Чем могу помочь?");
        assertThat(message.getSenderRole()).isEqualTo(ChatMessage.ROLE_AI);
        assertThat(message.getSender()).isEqualTo(ChatMessage.ROLE_AI);
    }

    @Test
    @DisplayName("Should normalize role names (assistant -> ai, uppercase -> lowercase)")
    void shouldNormalizeRoleNames() {
        ChatSession session = new ChatSession();

        ChatMessage m1 = new ChatMessage(session, "assistant", "test");
        assertThat(m1.getSenderRole()).isEqualTo(ChatMessage.ROLE_AI);

        ChatMessage m2 = new ChatMessage(session, "USER", "test");
        assertThat(m2.getSenderRole()).isEqualTo(ChatMessage.ROLE_USER);

        ChatMessage m3 = new ChatMessage(session, "  AI  ", "test");
        assertThat(m3.getSenderRole()).isEqualTo(ChatMessage.ROLE_AI);

        ChatMessage m4 = new ChatMessage(session, "", "test");
        assertThat(m4.getSenderRole()).isEqualTo(ChatMessage.ROLE_USER);

        ChatMessage m5 = new ChatMessage(session, null, "test");
        assertThat(m5.getSenderRole()).isEqualTo(ChatMessage.ROLE_USER);
    }

    @Test
    @DisplayName("Getters and setter aliases should stay synchronized")
    void aliasesShouldStaySynchronized() {
        ChatMessage message = new ChatMessage();
        message.setMessageContent("Content via alias");
        assertThat(message.getContent()).isEqualTo("Content via alias");

        message.setSender("ai");
        assertThat(message.getSenderRole()).isEqualTo("ai");

        LocalDateTime now = LocalDateTime.now();
        message.setTimestamp(now);
        assertThat(message.getTimestamp()).isEqualTo(now);
    }

    @Test
    @DisplayName("equals and hashCode should be based on ID identity")
    void equalsAndHashCodeShouldFollowJpaBestPractices() {
        ChatSession session = new ChatSession();
        LocalDateTime now = LocalDateTime.now();

        ChatMessage m1 = new ChatMessage(1L, session, "user", "msg1", now);
        ChatMessage m2 = new ChatMessage(1L, session, "ai", "msg2", now);
        ChatMessage m3 = new ChatMessage(2L, session, "user", "msg1", now);

        assertThat(m1).isEqualTo(m2);
        assertThat(m1).isNotEqualTo(m3);
        assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
    }

    @Test
    @DisplayName("toString should not cause recursive loops")
    void toStringShouldBeSafe() {
        ChatSession session = new ChatSession();
        ChatMessage message = new ChatMessage(5L, session, "user", "Длинный текст", LocalDateTime.now());

        String str = message.toString();
        assertThat(str).contains("id=5").contains("senderRole='user'").contains("contentLength=13");
    }
}
