-- Yaxin.az: issue reporting core (issues, support, timeline, comments)
CREATE TABLE issues (
    id                  BIGSERIAL PRIMARY KEY,
    community_id        BIGINT        NOT NULL REFERENCES communities (id),
    created_by          BIGINT        NOT NULL REFERENCES users (id),
    title               VARCHAR(200)  NOT NULL,
    description         VARCHAR(4000) NOT NULL,
    ai_summary          VARCHAR(2000),
    category            VARCHAR(30)   NOT NULL,
    priority            VARCHAR(20)   NOT NULL,
    status              VARCHAR(30)   NOT NULL,
    building_or_location VARCHAR(255),
    image_url           VARCHAR(500),
    stale               BOOLEAN       NOT NULL DEFAULT FALSE,
    escalated           BOOLEAN       NOT NULL DEFAULT FALSE,
    resolved_at         TIMESTAMP,
    version             BIGINT        NOT NULL DEFAULT 0,
    deleted             BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMP,
    deleted_by          BIGINT,
    created_at          TIMESTAMP     NOT NULL,
    updated_at          TIMESTAMP     NOT NULL
);

CREATE INDEX idx_issues_community ON issues (community_id);
CREATE INDEX idx_issues_status ON issues (status);
CREATE INDEX idx_issues_priority ON issues (priority);
CREATE INDEX idx_issues_created_at ON issues (created_at);

CREATE TABLE issue_supports (
    id          BIGSERIAL PRIMARY KEY,
    issue_id    BIGINT    NOT NULL REFERENCES issues (id),
    user_id     BIGINT    NOT NULL REFERENCES users (id),
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,
    CONSTRAINT uk_issue_support_issue_user UNIQUE (issue_id, user_id)
);

CREATE TABLE issue_activities (
    id            BIGSERIAL PRIMARY KEY,
    issue_id      BIGINT       NOT NULL REFERENCES issues (id),
    type          VARCHAR(30)  NOT NULL,
    message       VARCHAR(500) NOT NULL,
    actor_user_id BIGINT,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL
);

CREATE INDEX idx_issue_activities_issue ON issue_activities (issue_id);

CREATE TABLE issue_comments (
    id          BIGSERIAL PRIMARY KEY,
    issue_id    BIGINT        NOT NULL REFERENCES issues (id),
    author_id   BIGINT        NOT NULL REFERENCES users (id),
    content     VARCHAR(2000) NOT NULL,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL
);

CREATE INDEX idx_issue_comments_issue ON issue_comments (issue_id);
