package com.ghInternship.GHInternship2026.repository;

import com.ghInternship.GHInternship2026.entity.ChatMessage;
import com.ghInternship.GHInternship2026.entity.ChatSession;
import com.ghInternship.GHInternship2026.entity.User;
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
 * Комплексный тест персистентности на базе {@link DataJpaTest}.
 * Демонстрирует стратегию тестирования Data JPA (Repository Tests),
 * описанную в требованиях и Challenge Consideration:
 *
 * 1. Использование {@link DataJpaTest} с {@link TestEntityManager} для проверки сохранения
 *    и извлечения сущностей с контролем транзакций (автоматический rollback после каждого теста).
 * 2. По умолчанию Spring Boot использует быструю in-memory H2 базу данных для изолированных прогонов в CI/CD.
 * 3. Для реального PostgreSQL может применяться аннотация {@code @AutoConfigureTestDatabase(replace = Replace.NONE)}
 *    в связке с Testcontainers или внешним инстансом PostgreSQL 16.
 */
@DataJpaTest
class DataJpaTestPersistenceStrategyTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Test
    @DisplayName("@DataJpaTest: should persist User, ChatSession, and ChatMessage with correct foreign keys")
    void shouldPersistAndRetrieveEntitiesWithForeignKeys() {
        // 1. Создаем и сохраняем владельца
        User owner = new User("jpa_slice_user", "secure_hash_pwd");
        owner = entityManager.persistFlushFind(owner);
        assertThat(owner.getId()).isNotNull();

        // 2. Создаем сессию диалога
        ChatSession session = new ChatSession(owner, "Архитектура Spring Data JPA");
        session = entityManager.persistFlushFind(session);
        assertThat(session.getId()).isNotNull();
        assertThat(session.getUser().getId()).isEqualTo(owner.getId());

        // 3. Создаем сообщения: пользователя и AI
        ChatMessage userMsg = ChatMessage.userMessage(session, "Как работает @DataJpaTest?");
        ChatMessage aiMsg = ChatMessage.aiMessage(session, "@DataJpaTest конфигурирует JPA-срез с TestEntityManager.");

        entityManager.persist(userMsg);
        entityManager.persist(aiMsg);
        entityManager.flush();
        entityManager.clear();

        // 4. Проверяем извлечение через репозитории
        Optional<ChatSession> retrievedSessionOpt = chatSessionRepository.findByIdWithUser(session.getId());
        assertThat(retrievedSessionOpt).isPresent();
        ChatSession retrievedSession = retrievedSessionOpt.get();
        assertThat(retrievedSession.getTitle()).isEqualTo("Архитектура Spring Data JPA");
        assertThat(retrievedSession.getUser().getUsername()).isEqualTo("jpa_slice_user");

        // 5. Проверяем извлечение сообщений в прямом хронологическом порядке
        List<ChatMessage> messages = chatMessageRepository.findBySessionOrderByTimestampAsc(retrievedSession);
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getSenderRole()).isEqualTo(ChatMessage.ROLE_USER);
        assertThat(messages.get(0).getContent()).isEqualTo("Как работает @DataJpaTest?");
        assertThat(messages.get(1).getSenderRole()).isEqualTo(ChatMessage.ROLE_AI);
        assertThat(messages.get(1).getContent()).isEqualTo("@DataJpaTest конфигурирует JPA-срез с TestEntityManager.");
    }

    @Test
    @DisplayName("@DataJpaTest: custom query findByUserOrderByMostRecentActivityDesc should handle multi-message sessions")
    void shouldSortSessionsByMostRecentMessageActivity() {
        User user = entityManager.persistFlushFind(new User("activity_user", "pwd123"));

        LocalDateTime base = LocalDateTime.now().minusDays(2);

        // Сессия 1 создана раньше
        ChatSession s1 = entityManager.persistFlushFind(new ChatSession(null, user, "Тема 1 (ранняя)", base, base));
        // Сессия 2 создана позже
        ChatSession s2 = entityManager.persistFlushFind(new ChatSession(null, user, "Тема 2 (поздняя)", base.plusDays(1), base.plusDays(1)));

        // В Сессию 1 добавляем свежее сообщение
        ChatMessage freshMsg = new ChatMessage(s1, "user", "Самый свежий вопрос", LocalDateTime.now());
        entityManager.persist(freshMsg);
        s1.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(s1);

        entityManager.flush();
        entityManager.clear();

        // JPQL выборка по последней активности
        List<ChatSession> sortedSessions = chatSessionRepository.findByUserOrderByMostRecentActivityDesc(user);
        assertThat(sortedSessions).hasSize(2);
        // Сессия 1 должна быть первой из-за недавнего сообщения
        assertThat(sortedSessions.get(0).getId()).isEqualTo(s1.getId());
        assertThat(sortedSessions.get(0).getTitle()).isEqualTo("Тема 1 (ранняя)");
    }
}

