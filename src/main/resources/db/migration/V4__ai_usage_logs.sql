-- Yaxin.az: AI usage observability (spec section 38)
CREATE TABLE ai_usage_logs (
    id          BIGSERIAL PRIMARY KEY,
    feature     VARCHAR(60)  NOT NULL,
    provider    VARCHAR(20)  NOT NULL,
    model       VARCHAR(60),
    status      VARCHAR(20)  NOT NULL,
    latency_ms  BIGINT       NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL
);

CREATE INDEX idx_ai_usage_logs_created_at ON ai_usage_logs (created_at);
CREATE INDEX idx_ai_usage_logs_status ON ai_usage_logs (status);
