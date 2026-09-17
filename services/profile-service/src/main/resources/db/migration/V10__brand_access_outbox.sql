CREATE TABLE brand_access_outbox (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL,
    status varchar(40) NOT NULL,
    occurred_at timestamptz NOT NULL DEFAULT now(),
    published_at timestamptz,
    attempt_count integer NOT NULL DEFAULT 0,
    next_attempt_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_brand_access_outbox_pending
    ON brand_access_outbox(next_attempt_at, occurred_at) WHERE published_at IS NULL;

-- All status changes, including administrative lock/disable and cleanup, must revoke access.
-- The trigger writes in the same transaction as the profile; rollback also rolls back the event.
CREATE FUNCTION enqueue_brand_access_change() RETURNS trigger AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO brand_access_outbox(user_id, status) VALUES (NEW.user_id, NEW.status);
    ELSIF NEW.status IS DISTINCT FROM OLD.status THEN
        INSERT INTO brand_access_outbox(user_id, status) VALUES (NEW.user_id, NEW.status);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER brand_access_changed
AFTER INSERT OR UPDATE OF status ON brand_profiles
FOR EACH ROW EXECUTE FUNCTION enqueue_brand_access_change();

-- Reconcile accounts registered before ROLE_BRAND_PENDING was introduced.
INSERT INTO brand_access_outbox(user_id, status)
SELECT user_id, status FROM brand_profiles;
