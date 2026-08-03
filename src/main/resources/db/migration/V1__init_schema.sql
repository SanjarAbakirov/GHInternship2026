-- Baseline schema, matching the JPA entities as of the Flyway introduction.
-- Generated to match what Hibernate ddl-auto=update had already created in dev/prod.

CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    email       VARCHAR(100) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP,
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE chat_sessions (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    user_id     BIGINT NOT NULL,
    CONSTRAINT fk_chat_sessions_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_chat_sessions_user_id ON chat_sessions (user_id);

CREATE TABLE chat_messages (
    id          BIGSERIAL PRIMARY KEY,
    content     TEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    role        VARCHAR(16) NOT NULL,
    session_id  BIGINT NOT NULL,
    CONSTRAINT fk_chat_messages_session FOREIGN KEY (session_id) REFERENCES chat_sessions (id)
);

CREATE INDEX idx_chat_messages_session_id ON chat_messages (session_id);
CREATE INDEX idx_chat_messages_session_created_at ON chat_messages (session_id, created_at);
