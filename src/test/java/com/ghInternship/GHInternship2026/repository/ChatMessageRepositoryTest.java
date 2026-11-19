package com.ghInternship.GHInternship2026.repository;

import com.ghInternship.GHInternship2026.entity.ChatMessage;
import com.ghInternship.GHInternship2026.entity.ChatSession;
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
 * Slice-тесты Spring Data JPA репозитория {@link ChatMessageRepository} на базе {@link DataJpaTest}.
 * Проверяют сохранение и извлечение сообщений, прямой и обратный хронологический порядок,
 * получение последнего сообщения и каскадные операции в изолированной JPA-среде.
 */
@DataJpaTest
class ChatMessageRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private ChatSession testSession;

    @BeforeEach
    void setUp() {
        testUser = entityManager.persistFlushFind(new User("msg_repo_user_" + System.currentTimeMillis(), "hash_secret"));
        testSession = entityManager.persistFlushFind(new ChatSession(testUser, "Тестовая сессия"));
    }

    @Test
    @DisplayName("CRUD: should save, findById, count, and delete ChatMessage")
    void shouldPerformBasicCrudOperations() {
        ChatMessage message = ChatMessage.userMessage(testSession, "Привет, мир!");
        ChatMessage saved = chatMessageRepository.save(message);

        assertThat(saved.getId()).isNotNull();

        Optional<ChatMessage> found = chatMessageRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getContent()).isEqualTo("Привет, мир!");

        assertThat(chatMessageRepository.count()).isGreaterThanOrEqualTo(1);

        chatMessageRepository.delete(saved);
        assertThat(chatMessageRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("findBySessionOrderByTimestampAsc/Desc: should retrieve messages in chronological and reverse order")
    void shouldRetrieveMessagesInChronologicalAndReverseOrder() {
        LocalDateTime base = LocalDateTime.now().minusMinutes(10);

        ChatMessage m1 = new ChatMessage(testSession, "user", "Первое", base);
        ChatMessage m2 = new ChatMessage(testSession, "ai", "Второе", base.plusMinutes(2));
        ChatMessage m3 = new ChatMessage(testSession, "user", "Третье", base.plusMinutes(4));

        chatMessageRepository.saveAll(List.of(m1, m2, m3));
        entityManager.flush();
        entityManager.clear();

        // Прямой хронологический порядок
        List<ChatMessage> ascMessages = chatMessageRepository.findBySessionOrderByTimestampAsc(testSession);
        assertThat(ascMessages).hasSize(3);
        assertThat(ascMessages.get(0).getContent()).isEqualTo("Первое");
        assertThat(ascMessages.get(1).getContent()).isEqualTo("Второе");
        assertThat(ascMessages.get(2).getContent()).isEqualTo("Третье");

        // Обратный хронологический порядок (сначала новые)
        List<ChatMessage> descMessages = chatMessageRepository.findBySessionIdOrderByTimestampDesc(testSession.getId());
        assertThat(descMessages).hasSize(3);
        assertThat(descMessages.get(0).getContent()).isEqualTo("Третье");
        assertThat(descMessages.get(1).getContent()).isEqualTo("Второе");
        assertThat(descMessages.get(2).getContent()).isEqualTo("Первое");
    }

    @Test
    @DisplayName("findFirstBySessionOrderByTimestampDesc: should return the latest message in session")
    void shouldFindLatestMessageInSession() {
        LocalDateTime base = LocalDateTime.now().minusMinutes(5);

        chatMessageRepository.save(new ChatMessage(testSession, "user", "Вопрос", base));
        chatMessageRepository.save(new ChatMessage(testSession, "ai", "Последний ответ нейросети", base.plusMinutes(1)));

        entityManager.flush();
        entityManager.clear();

        Optional<ChatMessage> latest = chatMessageRepository.findFirstBySessionOrderByTimestampDesc(testSession);
        assertThat(latest).isPresent();
        assertThat(latest.get().getContent()).isEqualTo("Последний ответ нейросети");
        assertThat(latest.get().getSenderRole()).isEqualTo(ChatMessage.ROLE_AI);
    }

    @Test
    @DisplayName("countBySession: should count messages in session accurately")
    void shouldCountMessagesBySession() {
        assertThat(chatMessageRepository.countBySession(testSession)).isEqualTo(0);

        chatMessageRepository.save(ChatMessage.userMessage(testSession, "M1"));
        chatMessageRepository.save(ChatMessage.aiMessage(testSession, "M2"));

        assertThat(chatMessageRepository.countBySession(testSession)).isEqualTo(2);
        assertThat(chatMessageRepository.countBySessionId(testSession.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("deleteBySession: should remove all messages for specified session")
    void shouldDeleteMessagesBySession() {
        chatMessageRepository.save(ChatMessage.userMessage(testSession, "Сообщение для удаления"));
        entityManager.flush();
        entityManager.clear();

        assertThat(chatMessageRepository.countBySession(testSession)).isEqualTo(1);

        chatMessageRepository.deleteBySession(testSession);
        entityManager.flush();
        entityManager.clear();

        assertThat(chatMessageRepository.countBySession(testSession)).isEqualTo(0);
    }
}

