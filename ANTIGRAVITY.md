# Antigravity ↔ Cursor Sync (GHInternship2026)

> **Обязательный handoff-файл.**  
> Cursor и Antigravity читают этот файл **перед стартом любой работы** и обновляют его **после существенных изменений**.

---

## Правило для агентов (использовать как rule)

1. **Перед запуском задачи** обязательно прочитай:
   - `ANTIGRAVITY.md` (этот файл)
   - `AGENTS.md` (контекст проекта)
2. Если в разделе «Работа Antigravity» есть незакрытые пункты / конфликты / WIP — **не дублируй** и не ломай их без явной просьбы пользователя.
3. После значимых изменений (новые фичи, мерж, смена конфигов, смена AI-провайдера) **обнови** этот файл:
   - что сделано;
   - какие файлы затронуты;
   - что осталось;
   - риски / блокеры.
4. Секреты (`application-local.properties`, `.env`, API keys) **никогда** не писать сюда и не коммитить.

---

## Текущее состояние проекта

| Область | Статус |
|--------|--------|
| Backend | Java Spring Boot 3.4.1, Java 21 |
| DB runtime (default) | PostgreSQL profile `dev` (`spring.profiles.default=dev`) |
| DB local fallback | H2 profile `h2` (`./mvnw spring-boot:run -Dspring-boot.run.profiles=h2`) |
| AI provider | OpenRouter → `deepseek/deepseek-r1:free` (через `openai.api.*`) |
| Auth | JWT + Spring Security |
| Chat persistence | `ChatSession` / `ChatMessage` + save on `POST /api/chat` |
| Chat history API | `GET /api/chat/sessions`, `GET /api/chat/sessions/{sessionId}` |
| Tests | Backend `./mvnw test` зелёный (по последнему аудиту ~51) |

---

## Сводка работ (Week 6–7, Cursor)

### Week 6 (AI chat)
- `ChatService` → внешний chat-completions API (`RestTemplate`)
- JWT-protected `POST /api/chat`
- Ошибки AI → `AiServiceException` / 503 (позже добавлен fallback, чтобы UI не ломался на quota)

### Week 7 (PostgreSQL + persistence)
1. PostgreSQL JDBC + `docker-compose.yml` + `.env.example`
2. Datasource через Spring profiles (`application-dev.properties`, `application-prod.properties`, `application-h2.properties`)
3. Entities: `ChatSession`, `ChatMessage`
4. Repositories: `ChatSessionRepository`, `ChatMessageRepository` (+ order by last activity)
5. Persist on chat: create/reuse session, save user + AI messages, return `chatSessionId`
6. History endpoints + ownership checks
7. Hybrid default: Postgres `dev`, optional H2 profile

### Недавние правки на `main` (после persistence)
- OpenRouter + DeepSeek R1 free
- Resilient fallback при ошибке AI (без жёсткого 503 для UX)
- Hybrid profile mode docs в `AGENTS.md`

---

## Работа Antigravity

> Antigravity заполняет этот блок. Cursor обязан проверить его перед стартом.

### Последняя сессия Antigravity
- Дата: _(заполнить)_
- Ветка: _(заполнить)_
- Что сделано:
  - _(список)_
- Файлы:
  - _(пути)_
- Статус: `idle` / `in_progress` / `blocked`
- Блокеры / заметки:
  - _(если есть)_

### Открытые задачи Antigravity
- [ ] _(задача)_

---

## Работа Cursor (последнее)

- Дата: 2026-08-02
- Ветка: `cursor/antigravity-sync-rule-5271`
- Что сделано:
  - Создан `ANTIGRAVITY.md` (handoff + правило синхронизации)
  - Обновлён `AGENTS.md`: обязанность читать `ANTIGRAVITY.md` перед стартом
  - Добавлено Cursor rule `.cursor/rules/antigravity-sync.mdc`
- Статус: `in_progress` (docs/sync)

---

## Next Steps (общие)

1. Держать `ANTIGRAVITY.md` актуальным после каждой сессии агента.
2. При необходимости: Docker Postgres локально (`docker compose up -d`).
3. CorrelationId (MDC) на каждый запрос — ещё в планах.
4. CI/CD + Dockerfile — ещё в планах.
5. Дальнейшие задания Week 7+ — по запросу пользователя.

---

## Быстрые команды

```bash
git checkout main && git pull origin main
./mvnw test
./mvnw spring-boot:run                          # Postgres (dev)
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2   # H2 без Postgres
```
