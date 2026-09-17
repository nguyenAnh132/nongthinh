CREATE INDEX idx_posts_author_published
    ON posts(author_user_id, published_at DESC, id DESC)
    WHERE status = 'PUBLISHED'
      AND visibility = 'PUBLIC'
      AND deleted_at IS NULL;
