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
  - Для устранения ошибки подключения к PostgreSQL (`localhost:5432 Connection Refused`) в `docker-compose.yml` подготовлено окружение базы данных, скачан инсталлятор `Docker Desktop` для запуска контейнера `postgres:16-alpine`.
  - Все юнит и интеграционные тесты бэкенда (`./mvnw test`, 51 тест) и фронтенда (`npm test`, 20 тестов) проходят успешно.
## Планы (Next Steps)
1. Запуск PostgreSQL контейнера через Docker Desktop (`docker compose up -d`).
2. Реализовать генерацию `correlationId` (MDC) на старте каждого запроса для полноценной трассировки.
3. Подготовить проект к развертыванию (настройка CI/CD, Dockerfile и т.д.).

