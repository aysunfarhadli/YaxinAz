-- Yaxin.az: community feed, polls, events, lost & found (spec sections 46-49)

CREATE TABLE posts (
    id          BIGSERIAL PRIMARY KEY,
    community_id BIGINT       NOT NULL REFERENCES communities (id),
    author_id   BIGINT        NOT NULL REFERENCES users (id),
    post_type   VARCHAR(20)   NOT NULL,
    title       VARCHAR(200),
    content     VARCHAR(4000) NOT NULL,
    image_url   VARCHAR(500),
    pinned      BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted     BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMP,
    deleted_by  BIGINT,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL
);
CREATE INDEX idx_posts_community_created_at ON posts (community_id, created_at);
CREATE INDEX idx_posts_type ON posts (post_type);

CREATE TABLE post_likes (
    id          BIGSERIAL PRIMARY KEY,
    post_id     BIGINT    NOT NULL REFERENCES posts (id),
    user_id     BIGINT    NOT NULL REFERENCES users (id),
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,
    CONSTRAINT uk_post_like_post_user UNIQUE (post_id, user_id)
);

CREATE TABLE post_comments (
    id          BIGSERIAL PRIMARY KEY,
    post_id     BIGINT        NOT NULL REFERENCES posts (id),
    author_id   BIGINT        NOT NULL REFERENCES users (id),
    content     VARCHAR(2000) NOT NULL,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL
);
CREATE INDEX idx_post_comments_post ON post_comments (post_id);

CREATE TABLE polls (
    id          BIGSERIAL PRIMARY KEY,
    community_id BIGINT       NOT NULL REFERENCES communities (id),
    question    VARCHAR(500)  NOT NULL,
    created_by  BIGINT        NOT NULL REFERENCES users (id),
    expires_at  TIMESTAMP,
    active      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL
);
CREATE INDEX idx_polls_community ON polls (community_id);

CREATE TABLE poll_options (
    id          BIGSERIAL PRIMARY KEY,
    poll_id     BIGINT       NOT NULL REFERENCES polls (id),
    option_text VARCHAR(200) NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL
);
CREATE INDEX idx_poll_options_poll ON poll_options (poll_id);

CREATE TABLE poll_votes (
    id          BIGSERIAL PRIMARY KEY,
    poll_id     BIGINT    NOT NULL REFERENCES polls (id),
    option_id   BIGINT    NOT NULL REFERENCES poll_options (id),
    user_id     BIGINT    NOT NULL REFERENCES users (id),
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,
    CONSTRAINT uk_poll_vote_poll_user UNIQUE (poll_id, user_id)
);
CREATE INDEX idx_poll_votes_poll_user ON poll_votes (poll_id, user_id);

CREATE TABLE community_events (
    id          BIGSERIAL PRIMARY KEY,
    community_id BIGINT       NOT NULL REFERENCES communities (id),
    title       VARCHAR(200)  NOT NULL,
    description VARCHAR(2000),
    location    VARCHAR(255),
    start_time  TIMESTAMP     NOT NULL,
    end_time    TIMESTAMP     NOT NULL,
    capacity    INTEGER,
    created_by  BIGINT        NOT NULL REFERENCES users (id),
    deleted     BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMP,
    deleted_by  BIGINT,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL
);
CREATE INDEX idx_events_community_start ON community_events (community_id, start_time);

CREATE TABLE event_attendances (
    id          BIGSERIAL PRIMARY KEY,
    event_id    BIGINT      NOT NULL REFERENCES community_events (id),
    user_id     BIGINT      NOT NULL REFERENCES users (id),
    status      VARCHAR(20) NOT NULL,
    created_at  TIMESTAMP   NOT NULL,
    updated_at  TIMESTAMP   NOT NULL,
    CONSTRAINT uk_attendance_event_user UNIQUE (event_id, user_id)
);

CREATE TABLE lost_found_items (
    id          BIGSERIAL PRIMARY KEY,
    community_id BIGINT       NOT NULL REFERENCES communities (id),
    type        VARCHAR(10)   NOT NULL,
    title       VARCHAR(200)  NOT NULL,
    description VARCHAR(2000),
    image_url   VARCHAR(500),
    location    VARCHAR(255),
    status      VARCHAR(20)   NOT NULL,
    created_by  BIGINT        NOT NULL REFERENCES users (id),
    deleted     BOOLEAN       NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMP,
    deleted_by  BIGINT,
    created_at  TIMESTAMP     NOT NULL,
    updated_at  TIMESTAMP     NOT NULL
);
CREATE INDEX idx_lost_found_community ON lost_found_items (community_id);
CREATE INDEX idx_lost_found_type_status ON lost_found_items (type, status);
