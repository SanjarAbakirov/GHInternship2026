# Internship Project

This project was created as part of the **Internship course** to learn and practice backend development using **Java** and **Spring Boot**.

## PostgreSQL (Week 7)

The app uses **PostgreSQL** at runtime via Spring profiles. Unit tests use in-memory **H2** (`application-test.properties`).

| Profile | File | `ddl-auto` | When |
|---------|------|------------|------|
| `dev` (default) | `application-dev.properties` | `update` | local development |
| `prod` | `application-prod.properties` | `validate` | safer production-like runs |
| `test` | `application-test.properties` | `create-drop` | Maven tests (H2) |

### 1. Create local DB with Docker

```bash
cp .env.example .env
# edit POSTGRES_PASSWORD (and other values if needed)
docker compose up -d
```

This starts PostgreSQL 16 and creates a dedicated database/user from `.env`
(`POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD`).

### 2. Point Spring Boot at the DB (no secrets in git)

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
