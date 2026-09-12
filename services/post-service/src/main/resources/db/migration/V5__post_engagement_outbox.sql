ALTER TABLE posts ADD COLUMN reaction_version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE posts ADD COLUMN comment_version BIGINT NOT NULL DEFAULT 0;

CREATE TABLE post_outbox_events (
    id UUID PRIMARY KEY,
    sequence_no BIGINT GENERATED ALWAYS AS IDENTITY UNIQUE,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    schema_version INTEGER NOT NULL,
    payload JSONB NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_post_outbox_pending ON post_outbox_events(next_attempt_at, occurred_at)
    WHERE published_at IS NULL;
CREATE INDEX idx_post_outbox_aggregate_pending ON post_outbox_events(aggregate_id, occurred_at)
    WHERE published_at IS NULL;
