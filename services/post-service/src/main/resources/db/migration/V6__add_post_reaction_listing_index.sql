CREATE INDEX idx_post_reactions_post_created_id
    ON post_reactions (post_id, created_at DESC, id DESC);
