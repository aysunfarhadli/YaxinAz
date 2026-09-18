-- Yaxin.az: moderation queue + audit log (spec sections 63-64)
CREATE TABLE content_reports (
    id          BIGSERIAL PRIMARY KEY,
    reported_by BIGINT       NOT NULL REFERENCES users (id),
    content_type VARCHAR(30) NOT NULL,
    content_id  BIGINT       NOT NULL,
    reason      VARCHAR(30)  NOT NULL,
    description VARCHAR(1000),
    status      VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL
);
CREATE INDEX idx_content_reports_status ON content_reports (status);

CREATE TABLE audit_logs (
    id              BIGSERIAL PRIMARY KEY,
    actor_user_id   BIGINT,
    action_type     VARCHAR(40)  NOT NULL,
    resource_type   VARCHAR(60)  NOT NULL,
    resource_id     BIGINT,
    old_value       VARCHAR(500),
    new_value       VARCHAR(500),
    correlation_id  VARCHAR(60),
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL
);
CREATE INDEX idx_audit_logs_action_type ON audit_logs (action_type);
CREATE INDEX idx_audit_logs_actor ON audit_logs (actor_user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
