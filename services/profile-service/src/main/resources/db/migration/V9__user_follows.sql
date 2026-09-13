CREATE TABLE user_follows (
    id UUID PRIMARY KEY,
    follower_user_id UUID NOT NULL,
    followed_user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE (follower_user_id, followed_user_id),
    CHECK (follower_user_id <> followed_user_id)
);
CREATE INDEX idx_follows_followers ON user_follows(followed_user_id, created_at DESC, id DESC);
CREATE INDEX idx_follows_following ON user_follows(follower_user_id, created_at DESC, id DESC);

CREATE TABLE profile_notification_outbox (
    id UUID PRIMARY KEY,
    actor_user_id UUID NOT NULL,
    recipient_user_id UUID NOT NULL,
    notification_type VARCHAR(40) NOT NULL,
    entity_id UUID NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    attempt_count INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX idx_profile_notification_pending ON profile_notification_outbox(next_attempt_at, occurred_at)
    WHERE published_at IS NULL;
CREATE TABLE profile_processed_posts (post_id UUID PRIMARY KEY, processed_at TIMESTAMPTZ NOT NULL);
