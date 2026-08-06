# Project Context (GHInternship2026)

## Текущее состояние
- Проект: Java Spring Boot
- Последние действия: 
  - Проведен подробный аудит проекта (включая "атомный аудит" Python-инструментами и Java-плагинами: Checkstyle).
  - Найдены ошибки Checkstyle и падающие тесты (NullPointerException в `AuthControllerTest`).
  - Восстановлен случайно удаленный файл `UserService.java` из истории Git (он нужен для регистрации и аутентификации).
  - Был согласован и полностью выполнен план рефакторинга:
    - Удален проблемный Lombok из-за несовместимости с Java 25.
    - Починены тесты в `AuthControllerTest` (добавлена инициализация `JwtUtil`).
    - Секретный ключ JWT вынесен в `application.properties`.
    - `Controller.java` перемещен в пакет `controller`, создан `UserResponse` DTO для сокрытия паролей.
    - Добавлено логирование (`log.error`) в `GlobalExceptionHandler` и `ChatService`.
    - В `logback-spring.xml` добавлен токен `%X{correlationId}` для всех аппендеров.
    - В `SecurityConfig.java` настроен `AuthenticationEntryPoint` для возврата HTTP 401 вместо 403.
    - Реализованы Unit-тесты для `ChatController` (`@WithMockUser`, `MockMvc`) и `ChatService` (внедрен `RestTemplate`, использован `Mockito` для тестов без сети).
  - Упрощен `API Key Management` для 100% соответствия заданию Week 6 (игнорируется `application.properties`, упрощен `@Value`).
  - Переписан компонент чата на фронтенде (сохранение динамической истории сообщений).
  - Реализованы сущности `ChatSession` и `ChatMessage`, репозитории `ChatSessionRepository` и `ChatMessageRepository`.
  - В `ChatService` и `ChatController` добавлены методы сохранения истории сообщений и получения списка сессий пользователя (`GET /api/chat/sessions`, `GET /api/chat/sessions/{sessionId}`).
  - Настроен Гибридный режим (Вариант 3): профиль PostgreSQL (`dev`) установлен профилем по умолчанию в `application.properties` (`spring.profiles.default=dev`). Сохранен профиль `application-h2.properties` для быстрого локального запуска на H2 командой `./mvnw spring-boot:run -Dspring-boot.run.profiles=h2`.
  - При необходимости запуск приложения с PostgreSQL по-прежнему доступен командой `./mvnw spring-boot:run` (по умолчанию) или с ясным указанием профиля `-Dspring-boot.run.profiles=dev`.
  - Все юнит и интеграционные тесты бэкенда (`./mvnw test`, 51 тест) и фронтенда (`npm test`, 20 тестов) проходят с нулем ошибок.
  - **AI chat feature, PHASE 1 (JPA-сущности):** `ChatSession`/`ChatMessage` переименованы в `Conversation`/`Message` (репозитории, сервис, DTO — тоже переименованы: `ConversationRepository`, `MessageRepository`, `ConversationResponse`, `MessageResponse`). В `Conversation` добавлено поле `modelName` (фиксируется при создании разговора из `openai.api.model`). Публичный JSON-контракт `/api/chat/**` не менялся (поле `chatSessionId` осталось прежним) — фронтенд трогать не пришлось. Добавлена Flyway-миграция `V2__rename_chat_to_conversation.sql` для переноса существующих Postgres-таблиц/колонок/констрейнтов на новые имена. Все 50 тестов и живой E2E через curl (register→login→chat→sessions→messages) подтверждают корректность.
  - **AI chat feature, PHASE 2 (репозитории):** В `Conversation` добавлено поле `updatedAt`, автоматически обновляемое при создании/изменении и при добавлении/удалении сообщений (`addMessage`/`removeMessage`). `ConversationRepository`/`MessageRepository` сокращены до методов, явно требуемых заданием (`findByUserIdOrderByUpdatedAtDesc`, `findByIdAndUserId`, `findByConversationIdOrderByCreatedAtAsc`, `deleteByConversationId`). Добавлена Flyway-миграция `V3__add_conversation_updated_at.sql` (добавляет и бэкафиллит колонку, создаёт индекс `idx_conversations_user_updated_at`). Слит в `main`.
  - **AI chat feature, PHASE 3 (DTO):** Обновлены/созданы DTO по заданию:
    - `ChatRequest`: поле `chatSessionId` переименовано в `conversationId`; добавлено опциональное поле `modelName` (применяется только при создании нового разговора — задаёт `Conversation.modelName` вместо дефолтного `openai.api.model`).
    - `ChatResponse`: `chatSessionId` → `conversationId`; добавлены `conversationTitle`, `newConversation` (флаг создания нового разговора) и `timestamp` (время AI-ответа).
    - `ConversationResponse` (Conversation List DTO): добавлены `updatedAt`, `messageCount`, `lastMessagePreview` (последние два вычисляются через `MessageRepository.countByConversationId`/`findFirstByConversationIdOrderByCreatedAtDesc` — сознательный N+1 на текущем масштабе проекта, задокументирован в коде).
    - `MessageResponse` (Message DTO) — уже соответствовал требованиям, не менялся.
    - Новый `ConversationDetailResponse` (Conversation Detail DTO): метаданные разговора + `messageCount` + полный список `MessageResponse`. Теперь используется в `GET /api/chat/sessions/{sessionId}` вместо "голого" списка сообщений — это по пути исправило реальный баг рассинхронизации контракта (фронтенд уже ожидал объект `{ id, messages }`, а не массив).
    - Заодно исправлено рассинхронизацию имени поля `sessionId`/`chatSessionId` между фронтендом и бэкендом — оба теперь используют `conversationId`. Обновлены `chatApi.js`, `Chat.test.jsx`, `chatApi.test.js` на фронтенде.
    - Все 48 backend-тестов и 16 frontend-тестов проходят; живой E2E через curl (register→login→chat с modelName→continue→sessions→session detail) подтверждает корректность нового контракта.
## Планы (Next Steps)
1. Запуск PostgreSQL контейнера через Docker Desktop при необходимости на стороне разработчика.
2. Реализовать генерацию `correlationId` (MDC) на старте каждого запроса для полноценной трассировки.
3. Подготовить проект к развертыванию (настройка CI/CD, Dockerfile и т.д.).
4. Дальнейшие фазы фичи AI-чата (Phase 1–3 выполнены; уточнить у пользователя содержание Phase 4+, вероятно — обновление сервисного/контроллерного слоя под новые DTO, что уже частично сделано в рамках Phase 3).








