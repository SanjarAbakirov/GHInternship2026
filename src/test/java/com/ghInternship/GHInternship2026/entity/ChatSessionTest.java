package com.ghInternship.GHInternship2026.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Модульные тесты для JPA-сущности {@link ChatSession}.
 */
class ChatSessionTest {

    @Test
    @DisplayName("Should create ChatSession with default title when title is not provided")
    void shouldCreateSessionWithDefaultTitle() {
        User user = new User("test_user", "hash");
        ChatSession session = new ChatSession(user);

        assertThat(session.getUser()).isEqualTo(user);
        assertThat(session.getTitle()).isEqualTo(ChatSession.DEFAULT_TITLE);
        assertThat(session.getCreatedAt()).isNotNull();
        assertThat(session.getTimestamp()).isEqualTo(session.getCreatedAt());
        assertThat(session.getMessages()).isEmpty();
    }

    @Test
    @DisplayName("Should create ChatSession with custom title")
    void shouldCreateSessionWithCustomTitle() {
        User user = new User("alice", "hash");
        ChatSession session = new ChatSession(user, "Вопрос про Java");

        assertThat(session.getTitle()).isEqualTo("Вопрос про Java");
    }

    @Test
    @DisplayName("Should fallback to default title if blank title is passed")
    void shouldFallbackToDefaultTitleIfBlank() {
        User user = new User("bob", "hash");
        ChatSession session1 = new ChatSession(user, "   ");
        ChatSession session2 = new ChatSession(user, null);

        assertThat(session1.getTitle()).isEqualTo(ChatSession.DEFAULT_TITLE);
        assertThat(session2.getTitle()).isEqualTo(ChatSession.DEFAULT_TITLE);
    }

    @Test
    @DisplayName("generateTitleFromMessage should truncate long messages and clean whitespace")
    void shouldGenerateTitleFromMessage() {
        assertThat(ChatSession.generateTitleFromMessage(null)).isEqualTo(ChatSession.DEFAULT_TITLE);
        assertThat(ChatSession.generateTitleFromMessage("")).isEqualTo(ChatSession.DEFAULT_TITLE);
        assertThat(ChatSession.generateTitleFromMessage("   \n\t ")).isEqualTo(ChatSession.DEFAULT_TITLE);

        String shortMsg = "Как настроить PostgreSQL?";
        assertThat(ChatSession.generateTitleFromMessage(shortMsg)).isEqualTo(shortMsg);

        String longMsg = "Расскажи мне подробную историю создания языка программирования Java с момента проекта Oak в Sun Microsystems до наших дней";
        String generated = ChatSession.generateTitleFromMessage(longMsg);
        assertThat(generated).endsWith("...");
        assertThat(generated.length()).isLessThanOrEqualTo(53);
    }

    @Test
    @DisplayName("addMessage and removeMessage should manage bidirectional association")
    void shouldManageBidirectionalMessagesAssociation() {
        ChatSession session = new ChatSession();
        ChatMessage message1 = new ChatMessage();
        message1.setContent("Привет");
        message1.setSenderRole("user");

        session.addMessage(message1);
        assertThat(session.getMessages()).containsExactly(message1);
        assertThat(message1.getSession()).isEqualTo(session);

        session.removeMessage(message1);
        assertThat(session.getMessages()).isEmpty();
        assertThat(message1.getSession()).isNull();
    }

    @Test
    @DisplayName("equals and hashCode should be based on ID identity")
    void equalsAndHashCodeShouldFollowJpaBestPractices() {
        User user = new User("user", "hash");
        ChatSession s1 = new ChatSession(1L, user, "Title 1", LocalDateTime.now());
        ChatSession s2 = new ChatSession(1L, user, "Title 2", LocalDateTime.now());
        ChatSession s3 = new ChatSession(2L, user, "Title 1", LocalDateTime.now());

        assertThat(s1).isEqualTo(s2);
        assertThat(s1).isNotEqualTo(s3);
        assertThat(s1.hashCode()).isEqualTo(s2.hashCode());
    }

    @Test
    @DisplayName("toString should not throw StackOverflowError and contain basic session info")
    void toStringShouldBeSafe() {
        User user = new User("user", "hash");
        ChatSession session = new ChatSession(10L, user, "Session 10", LocalDateTime.now());
        session.addMessage(ChatMessage.userMessage(session, "Hello"));

        String str = session.toString();
        assertThat(str).contains("id=10").contains("Session 10").contains("messagesCount=1");
    }
}

