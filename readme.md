# Internship Project

This project was created as part of the **Internship course** to learn and practice backend development using **Java** and **Spring Boot**.

## PostgreSQL & schema migrations (Week 7+)

The app uses **PostgreSQL** at runtime via Spring profiles, with the schema owned by
**Flyway** migrations (`src/main/resources/db/migration`). Unit tests use in-memory **H2**
(`application-test.properties`) with Hibernate managing the throwaway schema instead.

| Profile | File | `ddl-auto` | Flyway | When |
|---------|------|------------|--------|------|
| `dev` (default) | `application-dev.properties` | `validate` | enabled | local development |
| `prod` | `application-prod.properties` | `validate` | enabled | production-like runs |
| `h2` | `application-h2.properties` | `update` | disabled | local dev without Postgres |
| `test` | `application-test.properties` | `create-drop` | disabled | Maven tests (H2) |

Adding a new column/table? Add a new `V<N>__description.sql` file under
`src/main/resources/db/migration` — never edit an already-applied migration file.

### 1. Create local DB with Docker

```bash
cp .env.example .env
# edit POSTGRES_PASSWORD, JWT_SECRET (and other values if needed)
docker compose up -d
```

This starts PostgreSQL 16 (creating a dedicated database/user from `.env`) and the Spring Boot
app itself (built from the repo `Dockerfile`), wired together on the same Docker network.
Run `docker compose up -d postgres` instead if you only want the database and prefer to run
the app locally via `./mvnw spring-boot:run`.

### 2. Point Spring Boot at the DB (no secrets in git)

The `dev` profile no longer falls back to a default password — you must provide one via
`POSTGRES_PASSWORD` / `SPRING_DATASOURCE_PASSWORD` env vars, or gitignored `application-local.properties`.
Either export env vars, or put them in gitignored `application-local.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/internship_db
spring.datasource.username=internship_user
spring.datasource.password=your_password_here
jwt.secret=your_jwt_secret_at_least_256_bits_long
```

JDBC URL format: `jdbc:postgresql://host:port/database`

### 3. Run the app

```bash
# default profile is already "dev"
./mvnw spring-boot:run

# or explicitly:
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# production-like (requires SPRING_DATASOURCE_* env vars; schema must already exist):
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

See `application.properties.example` and `.env.example`.
