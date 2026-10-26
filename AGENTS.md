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
   - [x] Добавлены тесты: [`HelloControllerTest`](src/test/java/com/ghInternship/GHInternship2026/controller/HelloControllerTest.java), модульный [`AuthControllerTest`](src/test/java/com/ghInternship/GHInternship2026/controller/AuthControllerTest.java) (MockMvc standalone с моками Mockito), интеграционный [`AuthControllerIntegrationTest`](src/test/java/com/ghInternship/GHInternship2026/controller/AuthControllerIntegrationTest.java) (Spring Boot + MockMvc + H2), [`UserServiceTest`](src/test/java/com/ghInternship/GHInternship2026/service/UserServiceTest.java), [`UserServiceImplTest`](src/test/java/com/ghInternship/GHInternship2026/service/UserServiceImplTest.java), [`PasswordEncoderTest`](src/test/java/com/ghInternship/GHInternship2026/config/PasswordEncoderTest.java), [`UserRegistrationDtoTest`](src/test/java/com/ghInternship/GHInternship2026/dto/UserRegistrationDtoTest.java) и [`UserLoginDtoTest`](src/test/java/com/ghInternship/GHInternship2026/dto/UserLoginDtoTest.java).
   - [x] Настроен CI/CD пайплайн в [`.github/workflows/ci.yml`](.github/workflows/ci.yml) (GitHub Actions): триггеры на `push` и `pull_request` в ветку `main`, настройка JDK 21 Temurin с кэшированием зависимостей Maven, сборка `./mvnw clean verify` и сохранение Surefire отчетов.
3. **Неделя №5 (Comprehensive Development - Frontend Integration):**
   - [x] Настроена поддержка CORS в [`SecurityConfig`](src/main/java/com/ghInternship/GHInternship2026/config/SecurityConfig.java) для интеграции с ReactJS (порты 3000 и 5173, методы `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`).
   - [x] Добавлена обработка `HttpMessageNotReadableException` в [`GlobalExceptionHandler`](src/main/java/com/ghInternship/GHInternship2026/exception/GlobalExceptionHandler.java) с возвратом HTTP 400 Bad Request при отсутствующем или невалидном теле запроса.
   - [x] Реализованы расширенные тест-кейсы для пустых и некорректных полей при регистрации (`POST /api/auth/register`) в модульных [`AuthControllerTest`](src/test/java/com/ghInternship/GHInternship2026/controller/AuthControllerTest.java), интеграционных [`AuthControllerIntegrationTest`](src/test/java/com/ghInternship/GHInternship2026/controller/AuthControllerIntegrationTest.java) и DTO-тестах [`UserRegistrationDtoTest`](src/test/java/com/ghInternship/GHInternship2026/dto/UserRegistrationDtoTest.java).
   - [x] Создана директория [`frontend/`](frontend/) и инициализирован проект ReactJS на базе Vite 8 (`npm install`, React 19).
   - [x] Разработана современная премиальная дизайн-система Dark Glassmorphism на чистом Vanilla CSS в [`index.css`](frontend/src/index.css) и [`App.css`](frontend/src/App.css) с шрифтами Inter и Outfit, фоновыми градиентными сферами, индикатором надежности пароля и тост-уведомлениями.
   - [x] Реализован API-клиент [`api.js`](frontend/src/services/api.js) с поддержкой запросов к `POST /api/auth/register`, `POST /api/auth/login` и проверки соединения `GET /hello`.
   - [x] Реализованы первые страницы и компоненты фронтенда:
     - [`RegisterForm.jsx`](frontend/src/components/RegisterForm.jsx): форма регистрации с живой валидацией полей, индикатором надежности пароля и подтверждением пароля.
     - [`LoginForm.jsx`](frontend/src/components/LoginForm.jsx): форма входа с переключением видимости пароля и опцией «Запомнить меня».
     - [`Navbar.jsx`](frontend/src/components/Navbar.jsx): навигационная шапка с живым пульсирующим статусом доступности бэкенда Spring Boot (`http://localhost:8080`) и переключателем вкладок.
     - [`Dashboard.jsx`](frontend/src/components/Dashboard.jsx): интерактивный личный кабинет после авторизации с профилем пользователя, статусом BCrypt хэширования, кнопкой тестирования бэкенда и кнопкой выхода.
     - [`Notification.jsx`](frontend/src/components/Notification.jsx): всплывающие анимированные тост-уведомления об успехе или ошибках валидации/сервера.
     - [`App.jsx`](frontend/src/App.jsx): корневой оркестратор состояний, сессии и маршрутизации представлений.
   - [x] Проверены сборка и линтинг фронтенда (`npm run build` и `npm run lint` — 0 ошибок и 0 предупреждений) и прогон всех 71 тестов бэкенда (`./mvnw test` — SUCCESS).
   - [x] Связана форма регистрации фронтенда с эндпоинтом бэкенда `POST /api/auth/register`:
     - В [`RegisterForm.jsx`](frontend/src/components/RegisterForm.jsx) добавлены атрибуты `action="/api/auth/register"`, `method="POST"`, атрибуты `name="username"`, `name="password"`, `name="confirmPassword"`, а также поддержка `autoComplete`, `spellCheck="false"` и `autoCapitalize="none"`.
     - Настроен обработчик `handleSubmit`, формирующий payload `{ username, password }`, в точности соответствующий Java-рекорду [`UserRegistrationDto`](src/main/java/com/ghInternship/GHInternship2026/dto/UserRegistrationDto.java).
     - Добавлено подробное логирование действий пользователя и этапов отправки запроса в консоль браузера.
     - Запущены и проверены сервисы бэкенда (`http://localhost:8080`) и фронтенда (`http://localhost:5173`): подтверждена успешная регистрация (201 Created), обнаружение конфликтов дубликатов (409 Conflict) и валидация (400 Bad Request).

## Следующие шаги
- Генерация JWT-токенов при успешной аутентификации в `POST /api/auth/login`.
- Добавление `JwtAuthenticationFilter` в цепочку безопасности Spring Security.
- Настройка `UserDetailsService` для загрузки пользователей по логину.
- Сохранение JWT-токена в `localStorage`/`HttpOnly cookie` на фронтенде и прикрепление заголовка `Authorization: Bearer <token>` к защищенным запросам.



