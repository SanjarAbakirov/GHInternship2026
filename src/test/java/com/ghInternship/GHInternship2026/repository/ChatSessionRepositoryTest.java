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
 * Slice-тесты Spring Data JPA репозитория {@link ChatSessionRepository} на базе {@link DataJpaTest}.
 * Проверяют стандартные CRUD-операции, маппинг сущностей и кастомные запросы
 * в изолированной среде с использованием {@link TestEntityManager}.
 */
@DataJpaTest
class ChatSessionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        testUser = entityManager.persistFlushFind(new User("session_repo_user_" + System.currentTimeMillis(), "hash_123"));
        otherUser = entityManager.persistFlushFind(new User("session_other_user_" + System.currentTimeMillis(), "hash_456"));
    }

    @Test
    @DisplayName("CRUD: should save, findById, count, and delete ChatSession")
    void shouldPerformBasicCrudOperations() {
        ChatSession session = new ChatSession(testUser, "Диалог по Java");
        ChatSession saved = chatSessionRepository.save(session);
        assertThat(saved.getId()).isNotNull();

        Optional<ChatSession> found = chatSessionRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Диалог по Java");

        long count = chatSessionRepository.count();
        assertThat(count).isGreaterThanOrEqualTo(1);

        chatSessionRepository.delete(saved);
        assertThat(chatSessionRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("findByUserOrderByCreatedAtDesc: should order sessions by creation time descending")
    void shouldFindSessionsOrderedByCreatedAtDesc() {
        LocalDateTime base = LocalDateTime.now().minusHours(3);

        ChatSession s1 = new ChatSession(null, testUser, "Первая сессия", base);
        ChatSession s2 = new ChatSession(null, testUser, "Вторая сессия", base.plusHours(1));
        ChatSession s3 = new ChatSession(null, testUser, "Третья сессия", base.plusHours(2));

        chatSessionRepository.saveAll(List.of(s1, s2, s3));
        entityManager.flush();
        entityManager.clear();

        List<ChatSession> sessions = chatSessionRepository.findByUserOrderByCreatedAtDesc(testUser);
        assertThat(sessions).hasSize(3);
        assertThat(sessions.get(0).getTitle()).isEqualTo("Третья сессия");
        assertThat(sessions.get(1).getTitle()).isEqualTo("Вторая сессия");
        assertThat(sessions.get(2).getTitle()).isEqualTo("Первая сессия");
    }

    @Test
    @DisplayName("Challenge: findByUserOrderByMostRecentActivityDesc should order sessions by latest message activity")
    void shouldOrderSessionsByMostRecentMessageActivity() {
        LocalDateTime base = LocalDateTime.now().minusHours(5);

        // Сессия 1 создана раньше (5 часов назад)
        ChatSession session1 = new ChatSession(null, testUser, "Старая сессия", base, base);
        // Сессия 2 создана позже (3 часа назад)
        ChatSession session2 = new ChatSession(null, testUser, "Новая сессия", base.plusHours(2), base.plusHours(2));

        session1 = chatSessionRepository.save(session1);
        session2 = chatSessionRepository.save(session2);

        // В Сессию 1 отправлено новое сообщение только что (наиболее свежая активность!)
        ChatMessage recentMessage = new ChatMessage(session1, "user", "Свежий вопрос в старой теме", LocalDateTime.now());
        session1.addMessage(recentMessage);
        chatSessionRepository.save(session1);

        entityManager.flush();
        entityManager.clear();

        // Проверяем JPQL-запрос по наиболее недавней активности
        List<ChatSession> activeSessions = chatSessionRepository.findByUserOrderByMostRecentActivityDesc(testUser);
        assertThat(activeSessions).hasSize(2);
        // Сессия 1 должна быть ПЕРВОЙ, так как в ней было самое свежее сообщение
        assertThat(activeSessions.get(0).getId()).isEqualTo(session1.getId());
        assertThat(activeSessions.get(0).getTitle()).isEqualTo("Старая сессия");
        assertThat(activeSessions.get(1).getId()).isEqualTo(session2.getId());
    }

    @Test
    @DisplayName("Security: findByIdAndUser and existsByIdAndUser should ensure tenant isolation")
    void shouldEnsureSecurityAndTenantIsolation() {
        ChatSession session = chatSessionRepository.save(new ChatSession(testUser, "Приватная сессия"));
        entityManager.flush();
        entityManager.clear();

        assertThat(chatSessionRepository.findByIdAndUser(session.getId(), testUser)).isPresent();
        assertThat(chatSessionRepository.findByIdAndUserId(session.getId(), testUser.getId())).isPresent();
        assertThat(chatSessionRepository.existsByIdAndUser(session.getId(), testUser)).isTrue();
        assertThat(chatSessionRepository.existsByIdAndUserId(session.getId(), testUser.getId())).isTrue();

        // Чужой пользователь не должен находить сессию
        assertThat(chatSessionRepository.findByIdAndUser(session.getId(), otherUser)).isEmpty();
        assertThat(chatSessionRepository.findByIdAndUserId(session.getId(), otherUser.getId())).isEmpty();
        assertThat(chatSessionRepository.existsByIdAndUser(session.getId(), otherUser)).isFalse();
    }

    @Test
    @DisplayName("countByUser: should return exact number of sessions for user")
    void shouldCountSessionsByUser() {
        assertThat(chatSessionRepository.countByUser(testUser)).isEqualTo(0);

        chatSessionRepository.save(new ChatSession(testUser, "Диалог 1"));
        chatSessionRepository.save(new ChatSession(testUser, "Диалог 2"));
        chatSessionRepository.save(new ChatSession(otherUser, "Диалог чужого"));

        assertThat(chatSessionRepository.countByUser(testUser)).isEqualTo(2);
        assertThat(chatSessionRepository.countByUserId(testUser.getId())).isEqualTo(2);
        assertThat(chatSessionRepository.countByUser(otherUser)).isEqualTo(1);
    }

    @Test
    @DisplayName("findByUserOrderByUpdatedAtDesc: should order sessions by update time descending")
    void shouldFindSessionsOrderedByUpdatedAtDesc() {
        LocalDateTime base = LocalDateTime.now().minusHours(4);

        ChatSession s1 = new ChatSession(null, testUser, "Первая", base, base);
        ChatSession s2 = new ChatSession(null, testUser, "Вторая", base.plusHours(1), base.plusHours(1));
        ChatSession s3 = new ChatSession(null, testUser, "Третья", base.plusHours(2), base.plusHours(2));

        chatSessionRepository.saveAll(List.of(s1, s2, s3));

        // Обновляем сессию s1 более поздней датой
        s1.setUpdatedAt(LocalDateTime.now());
        chatSessionRepository.save(s1);

        entityManager.flush();
        entityManager.clear();

        List<ChatSession> result = chatSessionRepository.findByUserOrderByUpdatedAtDesc(testUser);
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTitle()).isEqualTo("Первая"); // s1 теперь самая свежая по updatedAt
    }

    @Test
    @DisplayName("findByIdWithUser: should eagerly fetch User entity and avoid N+1 / LazyInitializationException")
    void shouldFetchSessionWithUserEagerly() {
        ChatSession session = chatSessionRepository.save(new ChatSession(testUser, "Диалог с юзером"));
        entityManager.flush();
        entityManager.clear();

        Optional<ChatSession> found = chatSessionRepository.findByIdWithUser(session.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUser()).isNotNull();
        assertThat(found.get().getUser().getUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    @DisplayName("deleteByIdAndUser: should delete session only if owned by user")
    void shouldDeleteSessionOnlyIfOwnedByUser() {
        ChatSession session = chatSessionRepository.save(new ChatSession(testUser, "Удаляемая сессия"));
        entityManager.flush();
        entityManager.clear();

        // Попытка удалить чужим пользователем
        chatSessionRepository.deleteByIdAndUser(session.getId(), otherUser);
        entityManager.flush();
        assertThat(chatSessionRepository.findById(session.getId())).isPresent();

        // Успешное удаление владельцем
        chatSessionRepository.deleteByIdAndUser(session.getId(), testUser);
        entityManager.flush();
        assertThat(chatSessionRepository.findById(session.getId())).isEmpty();
    }
}

