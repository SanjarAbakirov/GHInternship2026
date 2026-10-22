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
   - [x] Реализован слой бизнес-логики: [`HelloService`](src/main/java/com/ghInternship/GHInternship2026/service/HelloService.java), выделен интерфейс [`UserService`](src/main/java/com/ghInternship/GHInternship2026/service/UserService.java) и реализация [`UserServiceImpl`](src/main/java/com/ghInternship/GHInternship2026/service/UserServiceImpl.java).
   - [x] Созданы DTO для регистрации, аутентификации и ответов: [`UserRegistrationDto`](src/main/java/com/ghInternship/GHInternship2026/dto/UserRegistrationDto.java), [`UserLoginDto`](src/main/java/com/ghInternship/GHInternship2026/dto/UserLoginDto.java), [`UserResponseDto`](src/main/java/com/ghInternship/GHInternship2026/dto/UserResponseDto.java), [`AuthResponseDto`](src/main/java/com/ghInternship/GHInternship2026/dto/AuthResponseDto.java) и [`ErrorResponseDto`](src/main/java/com/ghInternship/GHInternship2026/dto/ErrorResponseDto.java).
   - [x] Создано доменное исключение [`UserAlreadyExistsException`](src/main/java/com/ghInternship/GHInternship2026/exception/UserAlreadyExistsException.java) и глобальный обработчик [`GlobalExceptionHandler`](src/main/java/com/ghInternship/GHInternship2026/exception/GlobalExceptionHandler.java) (`@RestControllerAdvice`) с маппингом в HTTP 409 Conflict и HTTP 400 Bad Request.
   - [x] Настроено хэширование паролей: объявлен бин `PasswordEncoder` (`BCryptPasswordEncoder`) в [`SecurityConfig`](src/main/java/com/ghInternship/GHInternship2026/config/SecurityConfig.java), внедрен в [`UserServiceImpl`](src/main/java/com/ghInternship/GHInternship2026/service/UserServiceImpl.java) и [`AuthController`](src/main/java/com/ghInternship/GHInternship2026/controller/AuthController.java).
   - [x] Создан [`AuthController`](src/main/java/com/ghInternship/GHInternship2026/controller/AuthController.java) с публичными эндпоинтами `POST /api/auth/register` (HTTP 201 Created) и `POST /api/auth/login` (HTTP 200 OK / HTTP 401 Unauthorized).
   - [x] Обновлен [`SecurityConfig`](src/main/java/com/ghInternship/GHInternship2026/config/SecurityConfig.java): настроен публичный доступ `permitAll()` и отключен CSRF для `/api/auth/**`.
   - [x] Настроен и верифицирован тестовый стек Unit-тестирования в [`pom.xml`](pom.xml): явно подключен `spring-boot-starter-test` (`<scope>test</scope>`), предоставляющий JUnit 5 (Jupiter 6.0.1) и Mockito (5.20.0, `mockito-junit-jupiter`).
   - [x] Добавлены тесты: [`HelloControllerTest`](src/test/java/com/ghInternship/GHInternship2026/controller/HelloControllerTest.java), [`AuthControllerTest`](src/test/java/com/ghInternship/GHInternship2026/controller/AuthControllerTest.java), [`UserServiceTest`](src/test/java/com/ghInternship/GHInternship2026/service/UserServiceTest.java), [`PasswordEncoderTest`](src/test/java/com/ghInternship/GHInternship2026/config/PasswordEncoderTest.java), [`UserRegistrationDtoTest`](src/test/java/com/ghInternship/GHInternship2026/dto/UserRegistrationDtoTest.java) и [`UserLoginDtoTest`](src/test/java/com/ghInternship/GHInternship2026/dto/UserLoginDtoTest.java).


## Следующие шаги
- Генерация JWT-токенов при успешной аутентификации в `POST /api/auth/login`.
- Добавление `JwtAuthenticationFilter` в цепочку безопасности Spring Security.
- Настройка `UserDetailsService` для загрузки пользователей по логину.



