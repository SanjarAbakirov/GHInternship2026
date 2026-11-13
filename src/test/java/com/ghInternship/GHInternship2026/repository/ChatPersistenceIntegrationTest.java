package com.ghInternship.GHInternship2026.repository;

import com.ghInternship.GHInternship2026.entity.ChatMessage;
import com.ghInternship.GHInternship2026.entity.ChatSession;
import com.ghInternship.GHInternship2026.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты персистентности моделей чата:
 * проверяют сохранение {@link ChatSession} и {@link ChatMessage},
 * каскадные операции (CascadeType.ALL), удаление осиротевших записей (orphanRemoval)
 * и изоляцию данных между пользователями.
 */
@SpringBootTest
@Transactional
class ChatPersistenceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(new User("chat_persister_" + System.currentTimeMillis(), "hash_secret_123"));
    }

    @Test
    @DisplayName("Cascade Persist: saving ChatSession should automatically persist all associated ChatMessages")
    void shouldPersistSessionAndCascadeMessages() {
        ChatSession session = new ChatSession(testUser, "Диалог о Spring Boot");
        ChatMessage userMsg = ChatMessage.userMessage(session, "Что такое Spring Boot?");
        ChatMessage aiMsg = ChatMessage.aiMessage(session, "Spring Boot — это фреймворк для быстрой разработки приложений на Java.");

        session.addMessage(userMsg);
        session.addMessage(aiMsg);

        ChatSession savedSession = chatSessionRepository.save(session);
        entityManager.flush();
        entityManager.clear();

        assertThat(savedSession.getId()).isNotNull();

        // Проверяем извлечение из репозитория сессий
        ChatSession retrievedSession = chatSessionRepository.findById(savedSession.getId()).orElseThrow();
        assertThat(retrievedSession.getTitle()).isEqualTo("Диалог о Spring Boot");
        assertThat(retrievedSession.getMessages()).hasSize(2);

        // Проверяем, что сообщения действительно сохранены в репозитории сообщений с присвоенными ID
        List<ChatMessage> messages = chatMessageRepository.findBySessionOrderByTimestampAsc(retrievedSession);
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getId()).isNotNull();
        assertThat(messages.get(0).getSenderRole()).isEqualTo(ChatMessage.ROLE_USER);
        assertThat(messages.get(0).getContent()).isEqualTo("Что такое Spring Boot?");

        assertThat(messages.get(1).getId()).isNotNull();
        assertThat(messages.get(1).getSenderRole()).isEqualTo(ChatMessage.ROLE_AI);
        assertThat(messages.get(1).getContent()).contains("Spring Boot — это фреймворк");
    }

    @Test
    @DisplayName("Ordering: messages should be returned in chronological order (timestamp ASC)")
    void shouldRetrieveMessagesInChronologicalOrder() {
        ChatSession session = chatSessionRepository.save(new ChatSession(testUser, "Хронологический тест"));

        LocalDateTime baseTime = LocalDateTime.now().minusMinutes(10);
        ChatMessage m1 = new ChatMessage(session, "user", "Первое сообщение", baseTime);
        ChatMessage m2 = new ChatMessage(session, "ai", "Второй ответ", baseTime.plusMinutes(1));
        ChatMessage m3 = new ChatMessage(session, "user", "Третье сообщение", baseTime.plusMinutes(2));

        session.addMessage(m1);
        session.addMessage(m2);
        session.addMessage(m3);

        chatSessionRepository.save(session);
        entityManager.flush();
        entityManager.clear();

        List<ChatMessage> chronological = chatMessageRepository.findBySessionIdOrderByTimestampAsc(session.getId());
        assertThat(chronological).hasSize(3);
        assertThat(chronological.get(0).getContent()).isEqualTo("Первое сообщение");
        assertThat(chronological.get(1).getContent()).isEqualTo("Второй ответ");
        assertThat(chronological.get(2).getContent()).isEqualTo("Третье сообщение");
    }

    @Test
    @DisplayName("Cascade Delete: deleting ChatSession should automatically delete all its ChatMessages")
    void shouldCascadeDeleteMessagesWhenSessionIsDeleted() {
        ChatSession session = new ChatSession(testUser, "Сессия под удаление");
        session.addMessage(ChatMessage.userMessage(session, "Удали меня"));
        session.addMessage(ChatMessage.aiMessage(session, "Будет удалено вместе с сессией"));

        ChatSession savedSession = chatSessionRepository.save(session);
        Long sessionId = savedSession.getId();
        entityManager.flush();
        entityManager.clear();

        assertThat(chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId)).hasSize(2);

        // Удаляем сессию
        ChatSession toDelete = chatSessionRepository.findById(sessionId).orElseThrow();
        chatSessionRepository.delete(toDelete);
        entityManager.flush();
        entityManager.clear();

        // Проверяем, что удалилась и сессия, и все каскадные сообщения
        assertThat(chatSessionRepository.findById(sessionId)).isEmpty();
        assertThat(chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId)).isEmpty();
    }

    @Test
    @DisplayName("Orphan Removal: removing a message from session.messages should delete it from database")
    void shouldRemoveOrphanMessageWhenRemovedFromSession() {
        ChatSession session = new ChatSession(testUser, "Сессия с сиротой");
        ChatMessage msg1 = ChatMessage.userMessage(session, "Останется");
        ChatMessage msg2 = ChatMessage.aiMessage(session, "Будет удалено из коллекции");

        session.addMessage(msg1);
        session.addMessage(msg2);

        ChatSession saved = chatSessionRepository.save(session);
        entityManager.flush();
        entityManager.clear();

        ChatSession loaded = chatSessionRepository.findById(saved.getId()).orElseThrow();
        ChatMessage toRemove = loaded.getMessages().stream()
                .filter(m -> m.getContent().equals("Будет удалено из коллекции"))
                .findFirst()
                .orElseThrow();

        // Удаляем из коллекции сессии
        loaded.removeMessage(toRemove);
        chatSessionRepository.save(loaded);
        entityManager.flush();
        entityManager.clear();

        ChatSession reloaded = chatSessionRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getMessages()).hasSize(1);
        assertThat(reloaded.getMessages().get(0).getContent()).isEqualTo("Останется");
    }

    @Test
    @DisplayName("Security & Isolation: user B should not access user A's chat session")
    void shouldIsolateSessionsBetweenUsers() {
        User otherUser = userRepository.save(new User("other_persister_" + System.currentTimeMillis(), "hash_other"));

        ChatSession sessionA = chatSessionRepository.save(new ChatSession(testUser, "Секретный диалог пользователя А"));
        entityManager.flush();
        entityManager.clear();

        // Владелец (testUser) может найти сессию
        Optional<ChatSession> foundByOwner = chatSessionRepository.findByIdAndUser(sessionA.getId(), testUser);
        assertThat(foundByOwner).isPresent();

        // Другой пользователь (otherUser) не должен иметь доступа
        Optional<ChatSession> foundByOther = chatSessionRepository.findByIdAndUser(sessionA.getId(), otherUser);
        assertThat(foundByOther).isEmpty();
        assertThat(chatSessionRepository.findByIdAndUserId(sessionA.getId(), otherUser.getId())).isEmpty();
    }
}

