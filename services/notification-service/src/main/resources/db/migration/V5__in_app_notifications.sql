CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    recipient_user_id UUID NOT NULL,
    actor_user_id UUID NOT NULL,
    type VARCHAR(40) NOT NULL,
    entity_type VARCHAR(40) NOT NULL,
    entity_id UUID NOT NULL,
    source_event_id UUID NOT NULL UNIQUE,
    source_version BIGINT NOT NULL,
    payload JSONB NOT NULL,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_notifications_recipient_created ON notifications(recipient_user_id, created_at DESC, id DESC);
CREATE INDEX idx_notifications_recipient_read ON notifications(recipient_user_id, read_at);
CREATE UNIQUE INDEX uq_notification_reaction ON notifications(actor_user_id, recipient_user_id, entity_id, type)
    WHERE type = 'POST_REACTION';
CREATE TABLE notification_processed_events (event_id UUID PRIMARY KEY, processed_at TIMESTAMPTZ NOT NULL);
-- Per-user revisions let every SSE instance see committed changes, including reads in another tab.
CREATE TABLE notification_user_state (
    user_id UUID PRIMARY KEY,
    version BIGINT NOT NULL,
    event_id UUID NOT NULL,
    event_type VARCHAR(80) NOT NULL
);
