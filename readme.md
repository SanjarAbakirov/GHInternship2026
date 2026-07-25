# Internship Project

This project was created as part of the **Internship course** to learn and practice backend development using **Java** and **Spring Boot**.

## PostgreSQL (Week 7)

The app uses **PostgreSQL** at runtime. Unit tests use in-memory **H2** (`src/test/resources/application.properties`).

### 1. Create local DB with Docker

```bash
cp .env.example .env
# edit POSTGRES_PASSWORD (and other values if needed)
docker compose up -d
```

This starts PostgreSQL 16 and creates a dedicated database/user from `.env`
(`POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD`).

### 2. Point Spring Boot at the DB (no secrets in git)

Either export the same env vars, or put them in gitignored `application-local.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/internship_db
spring.datasource.username=internship_user
spring.datasource.password=your_password_here
jwt.secret=your_jwt_secret_at_least_256_bits_long
```

See `application.properties.example` and `.env.example`.
