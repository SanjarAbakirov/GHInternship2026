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
   - [x] Организована базовая многослойная структура пакетов (Package-by-Layer): `controller`, `service`, `repository`, `entity`, `config`.
   - [x] Настроена H2 in-memory база данных и H2 web console в [`application.properties`](src/main/resources/application.properties).
   - [x] Создана JPA сущность [`User`](src/main/java/com/ghInternship/GHInternship2026/entity/User.java) (`id`, `username`, `passwordHash`).
   - [x] Создан репозиторий [`UserRepository`](src/main/java/com/ghInternship/GHInternship2026/repository/UserRepository.java) (`JpaRepository<User, Long>`).
   - [x] Реализован слой бизнес-логики: [`HelloService`](src/main/java/com/ghInternship/GHInternship2026/service/HelloService.java) и [`UserService`](src/main/java/com/ghInternship/GHInternship2026/service/UserService.java).
   - [x] Добавлены тесты: [`HelloControllerTest`](src/test/java/com/ghInternship/GHInternship2026/controller/HelloControllerTest.java) и [`UserServiceTest`](src/test/java/com/ghInternship/GHInternship2026/service/UserServiceTest.java).

## Следующие шаги
- Расширение функционала аутентификации и регистрации пользователей (регистрация/логин, хеширование паролей BCrypt/Argon2).
- Добавление DTO (Data Transfer Objects) и валидации входящих запросов (`spring-boot-starter-validation`).
- Настройка полноценного UserDetailsService для интеграции с Spring Security.
