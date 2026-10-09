# Контекст проекта GHInternship2026

## Текущее состояние
- **Ветка:** `main`
- **Java:** 25.0.2 LTS / 21.0.10 LTS
- **Spring Boot:** 4.0.0
- **Сборщик:** Maven (`./mvnw`)

## Выполненные этапы
1. **Неделя №1 (Environment Setup & Project Initialization):**
   - Настроена среда: JDK, Maven (`mvn -v`), IDE (IntelliJ IDEA CE, Antigravity IDE).
   - Сгенерирован чистый проект со стартерами Spring Web, Security, Data JPA, H2, Lombok.
   - Реализован [`HelloController`](src/main/java/com/ghInternship/GHInternship2026/controller/HelloController.java) (`GET /hello`).
   - Настроен [`SecurityConfig`](src/main/java/com/ghInternship/GHInternship2026/config/SecurityConfig.java) с цепочкой `SecurityFilterChain`, разрешающей публичный доступ к `/hello`.
   - Проверена работоспособность через cURL (HTTP 200 `Hello World!`).
2. **Неделя №2 (Foundation & Data Layer):**
   - [x] Настроен Git-репозиторий и подключен GitHub remote (`origin/main`).
   - [x] Обновлен [`.gitignore`](.gitignore) с исключениями для логов, дампов, `.env`, `.DS_Store`.
   - [x] Создан файл [`README.md`](README.md) с полным описанием проекта и инструкцией по запуску.

## Следующие шаги
- Организация базовой структуры пакетов (`entity`, `repository`, `service`).
- Настройка H2 in-memory базы данных и H2 web console в `application.properties`.
- Создание JPA сущности `User` (`id`, `username`, `passwordHash`).
- Создание интерфейса `UserRepository` с наследованием от `JpaRepository<User, Long>`.
