# Project Context (GHInternship2026)

## Обязательное правило синхронизации (Cursor + Antigravity)

**Перед стартом любой задачи** агент обязан:

1. Прочитать [`ANTIGRAVITY.md`](./ANTIGRAVITY.md) — сводку работ и статус Antigravity/Cursor.
2. Прочитать этот файл (`AGENTS.md`) — контекст проекта.
3. Не перетирать WIP Antigravity, если в `ANTIGRAVITY.md` статус `in_progress` / есть блокеры, без явной просьбы пользователя.

**После существенных изменений** обновить `ANTIGRAVITY.md` (что сделано, файлы, статус, next steps).

Правило также закреплено в `.cursor/rules/antigravity-sync.mdc`.

---

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
  - Добавлен handoff-файл `ANTIGRAVITY.md` + Cursor rule для синхронизации с Antigravity перед/после работы.
## Планы (Next Steps)
1. Запуск PostgreSQL контейнера через Docker Desktop при необходимости на стороне разработчика.
2. Реализовать генерацию `correlationId` (MDC) на старте каждого запроса для полноценной трассировки.
3. Подготовить проект к развертыванию (настройка CI/CD, Dockerfile и т.д.).
