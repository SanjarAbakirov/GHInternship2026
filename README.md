# GHInternship2026

Backend-приложение на **Java & Spring Boot**, разрабатываемое в рамках программы стажировки **GH Internship 2026**.

---

## 🛠 Стек технологий

* **Язык программирования:** Java 21 / 25 (LTS)
* **Фреймворк:** Spring Boot 4.0
  * **Spring Web (MVC):** создание RESTful API эндпоинтов
  * **Spring Security:** защита приложения и управление доступом
  * **Spring Data JPA:** персистентность данных и работа с репозиториями
* **База данных:** PostgreSQL 16 (основная БД для персистентного хранения) + H2 Database (изолированные автотесты и CI)
* **Фронтенд:** React 19 + Vite 8 + React Router DOM + Vitest / React Testing Library
* **Инструмент сборки:** Apache Maven (с использованием `mvnw` wrapper)
* **Утилиты:** Project Lombok

---

## 📋 Предварительные требования (Prerequisites)

* **JDK:** версия 17 или новее (рекомендуется Java 21 LTS или 25 LTS).
* **Git:** установлен и настроен.
* *Maven устанавливать отдельно не требуется* — в проект встроен скрипт `mvnw`.

---

## 🚀 Сборка и запуск проекта

### 1. Клонирование репозитория
```bash
git clone https://github.com/SanjarAbakirov/GHInternship2026.git
cd GHInternship2026
```

### 2. Настройка PostgreSQL 16 и переменных окружения
Создайте локальные файлы конфигурации из шаблонов (они исключены из Git в `.gitignore`):
```bash
cp .env.example .env
cp src/main/resources/application.properties.example src/main/resources/application.properties
```
Запустите PostgreSQL 16 через Docker Compose или локально через Homebrew:
```bash
# Вариант A: Docker Compose
docker compose up -d

# Вариант B: Локальный PostgreSQL 16 (macOS / Homebrew)
brew services start postgresql@16
```
> При необходимости запуска без установленного PostgreSQL используйте временный H2-профиль: `./mvnw spring-boot:run -Dspring-boot.run.profiles=h2`.

### 3. Сборка и запуск тестов
```bash
# Компиляция исходного кода
./mvnw clean compile

# Запуск модульных тестов
./mvnw test
```

### 4. Запуск приложения
```bash
./mvnw spring-boot:run
```
Приложение будет доступно по адресу: `http://localhost:8080`

---

## 🔌 Доступные эндпоинты (API)

| Метод | URL | Описание | Доступ | Пример ответа |
| :---: | :--- | :--- | :---: | :--- |
| `GET` | `/hello` | Базовый проверочный эндпоинт | Публичный (без пароля) | `Hello World!` |

### Проверка через cURL:
```bash
curl -i http://localhost:8080/hello
```

---

## 📁 Структура проекта

```text
src/main/java/com/ghInternship/GHInternship2026/
├── GhInternship2026Application.java   # Главный класс запуска Spring Boot
├── config/
│   └── SecurityConfig.java            # Конфигурация Spring Security (открыт /hello, h2-console)
├── controller/
│   └── HelloController.java           # Контроллер с эндпоинтом GET /hello
├── entity/
│   └── User.java                      # JPA сущность пользователя
├── repository/
│   └── UserRepository.java            # Репозиторий доступа к данным (Spring Data JPA)
└── service/
    ├── HelloService.java              # Сервис бизнес-логики приветствия
    └── UserService.java               # Сервис бизнес-логики пользователей
```

---

## 📌 Прогресс по этапам стажировки

### Неделя 1: Environment Setup & Project Initialization ✅
- [x] Установка и настройка JDK 25/21, переменных `JAVA_HOME` и `PATH`.
- [x] Выбор и настройка IDE (IntelliJ IDEA CE, VS Code) и расширений для Java.
- [x] Настройка инструмента сборки Apache Maven и `mvnw`.
- [x] Генерация структуры проекта Spring Boot с необходимыми зависимостями.
- [x] Создание первого контроллера `HelloController` с эндпоинтом `GET /hello`.
- [x] Настройка `SecurityConfig` для открытия доступа к публичным эндпоинтам.
- [x] Размещение репозитория на GitHub.

### Неделя 2: Foundation & Data Layer ✅
- [x] Настройка Git-репозитория и ведение истории коммитов.
- [x] Настройка `.gitignore` (исключение `target/`, `.idea/`, `.vscode/`, `*.log`, `.env`, `.DS_Store`).
- [x] Создание документации проекта (`README.md`).
- [x] Базовая структура пакетов (`entity`, `repository`, `service`, `controller`, `config`).
- [x] Настройка in-memory базы данных H2 и веб-консоли в `application.properties`.
- [x] Создание JPA сущности `User` (`id`, `username`, `passwordHash`).
- [x] Создание интерфейса `UserRepository` (`JpaRepository<User, Long>`).
- [x] Создание сервисов `HelloService` и `UserService` с модульными тестами.

