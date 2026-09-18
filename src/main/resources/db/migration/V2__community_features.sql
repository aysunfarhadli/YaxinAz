-- Yaxin.az: communities + community memberships
CREATE TABLE communities (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150)  NOT NULL,
    description     VARCHAR(2000),
    type            VARCHAR(30)   NOT NULL,
    city            VARCHAR(100)  NOT NULL,
    district        VARCHAR(150),
    address         VARCHAR(255),
    cover_image_url VARCHAR(500),
    created_by      BIGINT        NOT NULL REFERENCES users (id),
    deleted         BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at      TIMESTAMP,
    deleted_by      BIGINT,
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP     NOT NULL
);

CREATE INDEX idx_communities_city ON communities (city);

CREATE TABLE community_memberships (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT        NOT NULL REFERENCES users (id),
    community_id    BIGINT        NOT NULL REFERENCES communities (id),
    status          VARCHAR(20)   NOT NULL,
    joined_at       TIMESTAMP,
    reviewed_at     TIMESTAMP,
    reviewed_by     BIGINT,
    created_at      TIMESTAMP     NOT NULL,
    updated_at      TIMESTAMP     NOT NULL,
    CONSTRAINT uk_membership_user_community UNIQUE (user_id, community_id)
);

CREATE INDEX idx_memberships_user_community ON community_memberships (user_id, community_id);
CREATE INDEX idx_memberships_community_status ON community_memberships (community_id, status);
