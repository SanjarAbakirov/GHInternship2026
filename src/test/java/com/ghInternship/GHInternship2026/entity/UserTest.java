package com.ghInternship.GHInternship2026.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Модульные тесты для JPA-сущности {@link User} в рамках Step 1.3.
 */
class UserTest {

    @Test
    @DisplayName("User should manage bidirectional relationship with ChatSession / Conversation")
    void shouldManageChatSessionsAndConversations() {
        User user = new User("alice", "hash123");
        ChatSession session1 = new ChatSession(user, "Диалог 1");
        ChatSession session2 = new ChatSession(user, "Диалог 2");

        user.addConversation(session1);
        user.addChatSession(session2);

        assertThat(user.getChatSessions()).containsExactly(session1, session2);
        assertThat(user.getConversations()).containsExactly(session1, session2);
        assertThat(session1.getUser()).isEqualTo(user);
        assertThat(session2.getUser()).isEqualTo(user);

        user.removeConversation(session1);
        assertThat(user.getConversations()).containsExactly(session2);
        assertThat(session1.getUser()).isNull();

        user.setConversations(List.of(session1));
        assertThat(user.getChatSessions()).containsExactly(session1);
    }

    @Test
    @DisplayName("User equals and hashCode should be based on username")
    void equalsAndHashCodeShouldBeBasedOnUsername() {
        User u1 = new User("bob", "hash1");
        User u2 = new User("bob", "hash2");
        User u3 = new User("charlie", "hash1");

        assertThat(u1).isEqualTo(u2);
        assertThat(u1).isNotEqualTo(u3);
        assertThat(u1.hashCode()).isEqualTo(u2.hashCode());
    }
}
