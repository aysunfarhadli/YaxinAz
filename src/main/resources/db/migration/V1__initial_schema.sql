-- Yaxin.az initial schema: users
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    first_name      VARCHAR(100)  NOT NULL,
    last_name       VARCHAR(100)  NOT NULL,
    email           VARCHAR(255)  NOT NULL,
    password        VARCHAR(255)  NOT NULL,
    role            VARCHAR(30)   NOT NULL,
    avatar_url      VARCHAR(500),
    preferred_language VARCHAR(10) NOT NULL DEFAULT 'EN',
    enabled         BOOLEAN       NOT NULL DEFAULT TRUE,
    deleted         BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    deleted_by      BIGINT,
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP     NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);
