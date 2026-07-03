# Project Context (GHInternship2026)

## Текущее состояние
- Проект: Java Spring Boot
- Последние действия: 
  - Проведен подробный аудит проекта (включая "атомный аудит" Python-инструментами и Java-плагинами: Checkstyle).
  - Найдены ошибки Checkstyle и падающие тесты (NullPointerException в `AuthControllerTest`).
  - Восстановлен случайно удаленный файл `UserService.java` из истории Git (он нужен для регистрации и аутентификации).
  - Был согласован план рефакторинга (`implementation_plan.md`), к которому планируется приступить в ближайшее время.

## Планы (Next Steps)
1. Починить тесты в `AuthControllerTest` (добавить моки или исправить NullPointerException).
2. Вынести секретный ключ JWT в `application.properties`.
3. Переместить `Controller.java` обратно в пакет `controller` и создать `UserResponse` DTO для сокрытия паролей.
4. Добавить нормальное логирование (`log.error`) в `GlobalExceptionHandler` и `ChatService`.
5. Починить `logback-spring.xml` (добавить `%X{correlationId}`).
