package com.ghInternship.GHInternship2026.repository;

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
 * Тесты среза репозитория {@link ConversationRepository} с использованием {@link DataJpaTest}.
 */
@DataJpaTest
class ConversationRepositoryTest {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User userAlice;
    private User userBob;

    @BeforeEach
    void setUp() {
        userAlice = new User("alice_conv", "hashed_pwd_1");
        entityManager.persist(userAlice);

        userBob = new User("bob_conv", "hashed_pwd_2");
        entityManager.persist(userBob);

        entityManager.flush();
    }

    @Test
    @DisplayName("findByUserIdOrderByUpdatedAtDesc should return conversations ordered by update time descending")
    void findByUserIdOrderByUpdatedAtDescShouldReturnOrderedConversations() {
        LocalDateTime now = LocalDateTime.now();

        ChatSession convOld = new ChatSession(userAlice, "Старый диалог");
        convOld.setCreatedAt(now.minusDays(5));
        convOld.setUpdatedAt(now.minusDays(5));
        entityManager.persist(convOld);

        ChatSession convRecent = new ChatSession(userAlice, "Недавний диалог");
        convRecent.setCreatedAt(now.minusDays(2));
        convRecent.setUpdatedAt(now.minusHours(1));
        entityManager.persist(convRecent);

        ChatSession convMid = new ChatSession(userAlice, "Средний диалог");
        convMid.setCreatedAt(now.minusDays(3));
        convMid.setUpdatedAt(now.minusDays(1));
        entityManager.persist(convMid);

        // Диалог другого пользователя
        ChatSession bobConv = new ChatSession(userBob, "Диалог Боба");
        bobConv.setCreatedAt(now);
        bobConv.setUpdatedAt(now);
        entityManager.persist(bobConv);

        entityManager.flush();
        entityManager.clear();

        List<ChatSession> aliceConversations = conversationRepository.findByUserIdOrderByUpdatedAtDesc(userAlice.getId());

        assertThat(aliceConversations).hasSize(3);
        assertThat(aliceConversations.get(0).getTitle()).isEqualTo("Недавний диалог");
        assertThat(aliceConversations.get(1).getTitle()).isEqualTo("Средний диалог");
        assertThat(aliceConversations.get(2).getTitle()).isEqualTo("Старый диалог");
    }

    @Test
    @DisplayName("findByIdAndUserId should find conversation when user owns it (ownership validation passes)")
    void findByIdAndUserIdShouldFindConversationWhenOwnerMatches() {
        ChatSession conv = new ChatSession(userAlice, "Секретный диалог Алисы");
        entityManager.persist(conv);
        entityManager.flush();
        entityManager.clear();

        Optional<ChatSession> result = conversationRepository.findByIdAndUserId(conv.getId(), userAlice.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Секретный диалог Алисы");
        assertThat(result.get().getUser().getId()).isEqualTo(userAlice.getId());
    }

    @Test
    @DisplayName("findByIdAndUserId should return empty when user is not the owner (ownership validation rejects access)")
    void findByIdAndUserIdShouldReturnEmptyWhenOwnerDoesNotMatch() {
        ChatSession conv = new ChatSession(userAlice, "Приватный диалог Алисы");
        entityManager.persist(conv);
        entityManager.flush();
        entityManager.clear();

        // Попытка Боба запросить диалог Алисы
        Optional<ChatSession> result = conversationRepository.findByIdAndUserId(conv.getId(), userBob.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByIdAndUserId should return true only for valid owner")
    void existsByIdAndUserIdShouldValidateOwnership() {
        ChatSession conv = new ChatSession(userAlice, "Тестовый диалог");
        entityManager.persist(conv);
        entityManager.flush();
        entityManager.clear();

        assertThat(conversationRepository.existsByIdAndUserId(conv.getId(), userAlice.getId())).isTrue();
        assertThat(conversationRepository.existsByIdAndUserId(conv.getId(), userBob.getId())).isFalse();
        assertThat(conversationRepository.existsByIdAndUserId(99999L, userAlice.getId())).isFalse();
    }

    @Test
    @DisplayName("Standard CRUD operations should work properly via ConversationRepository")
    void shouldSupportStandardCrudOperations() {
        ChatSession conv = new ChatSession(userAlice, "Новая беседа", "gpt-4o");
        ChatSession saved = conversationRepository.save(conv);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getModelName()).isEqualTo("gpt-4o");

        Optional<ChatSession> found = conversationRepository.findById(saved.getId());
        assertThat(found).isPresent();

        conversationRepository.delete(saved);
        entityManager.flush();

        assertThat(conversationRepository.findById(saved.getId())).isEmpty();
    }
}
