-- Yaxin.az: local service marketplace (spec sections 50-52)
CREATE TABLE provider_profiles (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT        NOT NULL UNIQUE REFERENCES users (id),
    business_name   VARCHAR(200)  NOT NULL,
    bio             VARCHAR(2000),
    service_area    VARCHAR(255),
    verified        BOOLEAN       NOT NULL DEFAULT FALSE,
    average_rating  DOUBLE PRECISION NOT NULL DEFAULT 0,
    review_count    INTEGER       NOT NULL DEFAULT 0,
    deleted         BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    deleted_by      BIGINT,
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP     NOT NULL
);

CREATE TABLE provider_categories (
    provider_id BIGINT      NOT NULL REFERENCES provider_profiles (id),
    category    VARCHAR(30) NOT NULL
);
CREATE INDEX idx_provider_categories_category ON provider_categories (category);

CREATE TABLE service_requests (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT        NOT NULL REFERENCES users (id),
    provider_id     BIGINT        NOT NULL REFERENCES provider_profiles (id),
    category        VARCHAR(30)   NOT NULL,
    description     VARCHAR(2000) NOT NULL,
    status          VARCHAR(20)   NOT NULL,
    completed_at    TIMESTAMP,
    version         BIGINT        NOT NULL DEFAULT 0,
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP     NOT NULL
);
CREATE INDEX idx_service_requests_provider_status ON service_requests (provider_id, status);
CREATE INDEX idx_service_requests_customer ON service_requests (customer_id);

CREATE TABLE reviews (
    id                  BIGSERIAL PRIMARY KEY,
    service_request_id  BIGINT       NOT NULL UNIQUE REFERENCES service_requests (id),
    author_id           BIGINT       NOT NULL REFERENCES users (id),
    provider_id         BIGINT       NOT NULL REFERENCES provider_profiles (id),
    rating              INTEGER      NOT NULL,
    comment             VARCHAR(2000),
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL
);
CREATE INDEX idx_reviews_provider ON reviews (provider_id);
