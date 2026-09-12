CREATE INDEX idx_posts_created_active
    ON posts(created_at DESC, id DESC)
    WHERE deleted_at IS NULL;
