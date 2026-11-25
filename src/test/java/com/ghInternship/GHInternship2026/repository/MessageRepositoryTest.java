package com.ghInternship.GHInternship2026.repository;

import com.ghInternship.GHInternship2026.entity.ChatMessage;
import com.ghInternship.GHInternship2026.entity.ChatSession;
import com.ghInternship.GHInternship2026.entity.Message;
import com.ghInternship.GHInternship2026.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Slice-тесты Spring Data JPA репозитория {@link MessageRepository} на базе {@link DataJpaTest}.
 */
@DataJpaTest
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User testUser;
    private ChatSession conversationA;
    private ChatSession conversationB;

    @BeforeEach
    void setUp() {
        testUser = new User("message_tester", "hashed_password");
        entityManager.persist(testUser);

        conversationA = new ChatSession(testUser, "Беседа A");
        entityManager.persist(conversationA);

        conversationB = new ChatSession(testUser, "Беседа B");
        entityManager.persist(conversationB);

        entityManager.flush();
    }

    @Test
    @DisplayName("findBySessionIdOrderByTimestampAsc should return messages in chronological order (oldest first)")
    void findBySessionIdOrderByTimestampAscShouldReturnChronologicalMessages() {
        LocalDateTime baseTime = LocalDateTime.of(2026, 11, 26, 10, 0, 0);

        ChatMessage msg1 = Message.of(conversationA, "user", "Первый вопрос", baseTime);
        ChatMessage msg2 = Message.of(conversationA, "ai", "Первый ответ", baseTime.plusSeconds(5));
        ChatMessage msg3 = Message.of(conversationA, "user", "Второй вопрос", baseTime.plusSeconds(30));
        ChatMessage msg4 = Message.of(conversationA, "ai", "Второй ответ", baseTime.plusSeconds(35));

        // Добавим сообщение в другую беседу, чтобы убедиться в фильтрации
        ChatMessage otherConvMsg = Message.of(conversationB, "user", "Вопрос в беседе B", baseTime);

        // Намеренно сохраняем в БД в случайном порядке
        entityManager.persist(msg3);
        entityManager.persist(msg1);
        entityManager.persist(msg4);
        entityManager.persist(msg2);
        entityManager.persist(otherConvMsg);

        entityManager.flush();
        entityManager.clear();

        List<ChatMessage> messages = messageRepository.findBySessionIdOrderByTimestampAsc(conversationA.getId());

        assertThat(messages).hasSize(4);
        assertThat(messages.get(0).getContent()).isEqualTo("Первый вопрос");
        assertThat(messages.get(1).getContent()).isEqualTo("Первый ответ");
        assertThat(messages.get(2).getContent()).isEqualTo("Второй вопрос");
        assertThat(messages.get(3).getContent()).isEqualTo("Второй ответ");
        assertThat(messages.get(0).getTimestamp()).isBefore(messages.get(1).getTimestamp());
        assertThat(messages.get(1).getTimestamp()).isBefore(messages.get(2).getTimestamp());
        assertThat(messages.get(2).getTimestamp()).isBefore(messages.get(3).getTimestamp());
    }

    @Test
    @DisplayName("findByConversationId (JPQL) should return messages ordered chronologically")
    void findByConversationIdShouldReturnChronologicalMessages() {
        LocalDateTime now = LocalDateTime.now();
        ChatMessage m1 = Message.of(conversationA, "user", "Вопрос 1", now.minusMinutes(2));
        ChatMessage m2 = Message.of(conversationA, "ai", "Ответ 1", now.minusMinutes(1));

        entityManager.persist(m1);
        entityManager.persist(m2);
        entityManager.flush();
        entityManager.clear();

        List<ChatMessage> list = messageRepository.findByConversationId(conversationA.getId());
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getContent()).isEqualTo("Вопрос 1");
        assertThat(list.get(1).getContent()).isEqualTo("Ответ 1");
    }

    @Test
    @DisplayName("clearConversationHistory and deleteBySessionId should clear only target conversation messages")
    void shouldClearOnlyTargetConversationMessages() {
        ChatMessage msgA1 = Message.user(conversationA, "Сообщение в A-1");
        ChatMessage msgA2 = Message.ai(conversationA, "Сообщение в A-2");
        ChatMessage msgB1 = Message.user(conversationB, "Сообщение в B-1");

        entityManager.persist(msgA1);
        entityManager.persist(msgA2);
        entityManager.persist(msgB1);
        entityManager.flush();
        entityManager.clear();

        assertThat(messageRepository.countBySessionId(conversationA.getId())).isEqualTo(2);
        assertThat(messageRepository.countBySessionId(conversationB.getId())).isEqualTo(1);

        // Очищаем историю беседы А
        messageRepository.clearConversationHistory(conversationA.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(messageRepository.countBySessionId(conversationA.getId())).isEqualTo(0);
        assertThat(messageRepository.findBySessionIdOrderByTimestampAsc(conversationA.getId())).isEmpty();

        // Сообщения беседы B остались нетронутыми
        assertThat(messageRepository.countBySessionId(conversationB.getId())).isEqualTo(1);
        assertThat(messageRepository.findBySessionIdOrderByTimestampAsc(conversationB.getId())).hasSize(1);
    }

    @Test
    @DisplayName("findFirstBySessionIdOrderByTimestampDesc should find last message for preview")
    void shouldFindLastMessageForPreview() {
        LocalDateTime now = LocalDateTime.now();
        ChatMessage first = Message.of(conversationA, "user", "Первое", now.minusMinutes(5));
        ChatMessage last = Message.of(conversationA, "ai", "Самое последнее сообщение", now);

        entityManager.persist(first);
        entityManager.persist(last);
        entityManager.flush();
        entityManager.clear();

        Optional<ChatMessage> found = messageRepository.findFirstBySessionIdOrderByTimestampDesc(conversationA.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo("Самое последнее сообщение");
    }
}
