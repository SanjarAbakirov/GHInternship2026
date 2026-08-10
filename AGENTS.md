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
  - **AI chat feature, PHASE 4 (сервисный слой):** `ChatService` остаётся конкретным классом (без выделения `interface`, как и `UserService` — так принято во всём проекте), но его публичный контракт доработан по заданию:
    - `chat(...)` теперь перед вызовом AI подгружает полную историю разговора из БД (`messageRepository.findByConversationIdOrderByCreatedAtAsc`, с капом `MAX_HISTORY_MESSAGES=20`) и передаёт её в AI-запрос как multi-turn контекст (роли маппятся `ai`→`assistant`, `user`→`user`), а не только последнее сообщение — реальное поведенческое улучшение, не просто рефакторинг.
    - Добавлен `deleteConversation(username, conversationId)` — удаляет разговор с проверкой владения; сообщения удаляются каскадно через существующие `CascadeType.ALL`/`orphanRemoval=true` на `Conversation.messages`. Подключён новый эндпоинт `DELETE /api/chat/sessions/{sessionId}` → 204/404.
    - Выделены хелперы `toMessageResponse` (entity→DTO) и `capHistory` (ограничение размера истории для AI-запроса) — на что явно просило задание (Step 4.4).
    - `listSessions`/`getConversationDetail`/`getChatReply` — уже соответствовали спецификации Step 4.5/4.6 благодаря Phase 3, не менялись структурно.
    - Все 57 backend-тестов (было 48, +9 новых: multi-turn контекст с проверкой ролей в теле AI-запроса, удаление владельцем/не-владельцем, каскадное удаление сообщений через реальную H2-интеграцию) проходят; живой E2E через curl (2 хода в одном разговоре → GET detail с 4 сообщениями → DELETE 204 → GET 404 → пустой список) подтверждает корректность.
  - **AI chat feature, PHASE 5 (контроллеры):** Все 4 эндпоинта (`POST /api/chat`, `GET /api/chat/sessions`, `GET /api/chat/sessions/{id}`, `DELETE /api/chat/sessions/{id}`) уже существовали по итогам Phase 3–4; для Phase 5 добавлен единый приватный хелпер `ChatController.getAuthenticatedUsername()`, читающий имя пользователя из `SecurityContextHolder.getContext().getAuthentication()` вместо повторяющегося параметра `Authentication authentication` в каждом методе. `JwtAuthenticationFilter` уже кладёт `Authentication` в security context до вызова контроллера, а `SecurityConfig` требует аутентификацию для всех `/api/chat/**` — так что хелпер безопасен (никогда не увидит `null`) и работает одинаково в проде и в MockMvc-тестах. Все 57 тестов проходят; живой E2E (без токена → 401, с токеном → chat/list/detail/delete работают) подтверждает корректность.
  - **AI chat feature, PHASE 6 (обработка исключений):** Старый `ResourceNotFoundException` покрывал сразу три разных случая ("разговор не найден", "разговор чужой", "юзер не найден") одним 404 — по заданию Step 6.1 разбит на три отдельных класса:
    - `ConversationNotFoundException` (404) — разговора с таким id не существует вовсе.
    - `UnauthorizedConversationAccessException` (403) — разговор существует, но принадлежит другому пользователю. Раньше эти два случая объединялись через `findByIdAndUserId` в один "не найдено"; теперь `ChatService.requireOwnedConversation()` сперва ищет по `findById`, затем явно сверяет владельца — так что 404 и 403 стали различимы. Осознанный компромисс: это подтверждает существование чужого id, но даёт понятную REST-семантику (задокументировано в Javadoc класса).
    - `UserNotFoundException` (404) — аутентифицированный по JWT юзернейм не резолвится в `User` в базе (например, аккаунт удалён после выдачи токена).
    - `GlobalExceptionHandler` дополнен тремя обработчиками (Step 6.2), возвращающими `AuthResponse` с понятным `message` и соответствующим статусом — тот же паттерн, что уже использовался для `AiServiceException`/`DuplicateUserException`/`AuthenticationException`.
    - Заодно исправлена рассинхронизация на фронтенде: `getChatErrorMessage()` читал несуществующее поле `data.error` вместо реального `data.message` — теперь читает оба (сначала `message`).
    - Все 64 backend-теста (было 57, +7 новых: раздельные тесты на "не найдено" vs "не твой" для chat/getConversationDetail/deleteConversation) проходят; живой E2E через curl (userB читает/удаляет разговор userA → 403 с понятным сообщением; несуществующий id → 404; сам владелец всё ещё видит свой разговор после чужой попытки удаления) подтверждает корректность.
## Планы (Next Steps)
1. Запуск PostgreSQL контейнера через Docker Desktop при необходимости на стороне разработчика.
2. Реализовать генерацию `correlationId` (MDC) на старте каждого запроса для полноценной трассировки.
3. Подготовить проект к развертыванию (настройка CI/CD, Dockerfile и т.д.).
4. Дальнейшие фазы фичи AI-чата (Phase 1–6 выполнены; фронтенд пока не использует `DELETE /api/chat/sessions/{id}` — можно добавить кнопку удаления разговора в `ConversationSidebar`, если это часть следующей фазы).








