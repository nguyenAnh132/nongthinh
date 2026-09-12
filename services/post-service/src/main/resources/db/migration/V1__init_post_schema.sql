CREATE TABLE post_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_post_types_code UNIQUE (code),
    CONSTRAINT chk_post_types_code CHECK (
        length(trim(code)) > 0
        AND code = upper(code)
        AND code ~ '^[A-Z][A-Z0-9_]*$'
    ),
    CONSTRAINT chk_post_types_name CHECK (length(trim(name)) > 0),
    CONSTRAINT chk_post_types_display_order CHECK (display_order >= 0)
);

INSERT INTO post_types (code, name, display_order)
VALUES
    ('QUESTION', 'Hỏi đáp kỹ thuật', 10),
    ('EXPERIENCE', 'Chia sẻ kinh nghiệm', 20),
    ('WARNING', 'Cảnh báo mùa vụ', 30),
    ('PRICE', 'Thông tin giá nông sản', 40),
    ('EVENT', 'Sự kiện', 50);

CREATE TABLE post_topics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(180) NOT NULL,
    description VARCHAR(500),
    display_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_post_topics_slug UNIQUE (slug),
    CONSTRAINT chk_post_topics_name CHECK (length(trim(name)) > 0),
    CONSTRAINT chk_post_topics_slug CHECK (
        length(trim(slug)) > 0
        AND slug = lower(slug)
        AND slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'
    ),
    CONSTRAINT chk_post_topics_display_order CHECK (display_order >= 0)
);

CREATE TABLE posts (
    id UUID PRIMARY KEY,
    author_user_id UUID NOT NULL,
    post_type_id UUID NOT NULL,
    topic_id UUID,
    location_text VARCHAR(120),
    content VARCHAR(2000) NOT NULL,
    visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    status VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED',
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_posts_post_type FOREIGN KEY (post_type_id)
        REFERENCES post_types(id) ON DELETE RESTRICT,
    CONSTRAINT fk_posts_topic FOREIGN KEY (topic_id)
        REFERENCES post_topics(id) ON DELETE SET NULL,
    CONSTRAINT chk_posts_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'HIDDEN')),
    CONSTRAINT chk_posts_visibility CHECK (visibility IN ('PUBLIC', 'PRIVATE', 'FOLLOWERS')),
    CONSTRAINT chk_posts_content CHECK (
        length(trim(content)) BETWEEN 1 AND 2000
    ),
    CONSTRAINT chk_posts_location CHECK (
        location_text IS NULL OR length(trim(location_text)) > 0
    ),
    CONSTRAINT chk_posts_published_at CHECK (
        (status = 'DRAFT' AND published_at IS NULL)
        OR (status IN ('PUBLISHED', 'HIDDEN') AND published_at IS NOT NULL)
    ),
    CONSTRAINT chk_posts_deleted_at CHECK (
        deleted_at IS NULL OR deleted_at >= created_at
    )
);

CREATE TABLE post_media (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL,
    file_id UUID NOT NULL,
    media_type VARCHAR(30) NOT NULL,
    media_url VARCHAR(1000) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    width INTEGER,
    height INTEGER,
    size_bytes BIGINT,
    display_order INTEGER NOT NULL DEFAULT 0,
    caption VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_post_media_post FOREIGN KEY (post_id)
        REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT chk_post_media_type CHECK (media_type IN ('IMAGE', 'VIDEO')),
    CONSTRAINT chk_post_media_url CHECK (length(trim(media_url)) > 0),
    CONSTRAINT chk_post_media_content_type CHECK (
        (media_type = 'IMAGE' AND content_type LIKE 'image/%')
        OR (media_type = 'VIDEO' AND content_type LIKE 'video/%')
    ),
    CONSTRAINT chk_post_media_dimensions CHECK (
        (width IS NULL AND height IS NULL)
        OR (width > 0 AND height > 0)
    ),
    CONSTRAINT chk_post_media_size CHECK (size_bytes IS NULL OR size_bytes > 0),
    CONSTRAINT chk_post_media_display_order CHECK (display_order >= 0),
    CONSTRAINT chk_post_media_caption CHECK (
        caption IS NULL OR length(trim(caption)) > 0
    ),
    CONSTRAINT uq_post_media_post_file UNIQUE (post_id, file_id),
    CONSTRAINT uq_post_media_post_order UNIQUE (post_id, display_order)
);

-- crop_type_id is a logical reference to agri-catalog-service.
CREATE TABLE post_crop_types (
    post_id UUID NOT NULL,
    crop_type_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_post_crop_types PRIMARY KEY (post_id, crop_type_id),
    CONSTRAINT fk_post_crop_types_post FOREIGN KEY (post_id)
        REFERENCES posts(id) ON DELETE CASCADE
);

CREATE TABLE post_comments (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL,
    parent_comment_id UUID,
    author_user_id UUID NOT NULL,
    content VARCHAR(2000) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ,

    CONSTRAINT fk_post_comments_post FOREIGN KEY (post_id)
        REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT uq_post_comments_post_id UNIQUE (post_id, id),
    CONSTRAINT fk_post_comments_parent FOREIGN KEY (post_id, parent_comment_id)
        REFERENCES post_comments(post_id, id) ON DELETE NO ACTION,
    CONSTRAINT chk_post_comments_not_self_parent CHECK (
        parent_comment_id IS NULL OR parent_comment_id <> id
    ),
    CONSTRAINT chk_post_comments_status CHECK (status IN ('PUBLISHED', 'HIDDEN')),
    CONSTRAINT chk_post_comments_content CHECK (
        length(trim(content)) BETWEEN 1 AND 2000
    ),
    CONSTRAINT chk_post_comments_deleted_at CHECK (
        deleted_at IS NULL OR deleted_at >= created_at
    )
);

CREATE TABLE post_reactions (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL,
    actor_id UUID NOT NULL,
    reaction_type VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_post_reactions_post FOREIGN KEY (post_id)
        REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT chk_post_reactions_type CHECK (
        reaction_type IN ('LIKE', 'LOVE', 'HAHA', 'WOW', 'SAD', 'ANGRY')
    ),
    CONSTRAINT uq_post_reactions_actor UNIQUE (post_id, actor_id)
);

CREATE TABLE post_bookmarks (
    post_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_post_bookmarks PRIMARY KEY (post_id, user_id),
    CONSTRAINT fk_post_bookmarks_post FOREIGN KEY (post_id)
        REFERENCES posts(id) ON DELETE CASCADE
);

CREATE TABLE post_shares (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL,
    shared_by_user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_post_shares_post FOREIGN KEY (post_id)
        REFERENCES posts(id) ON DELETE CASCADE
);

CREATE TABLE post_reports (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL,
    reporter_id UUID NOT NULL,
    reason_code VARCHAR(50) NOT NULL,
    reason_detail TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    resolved_by UUID,
    resolved_at TIMESTAMPTZ,
    resolution_note TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_post_reports_post FOREIGN KEY (post_id)
        REFERENCES posts(id) ON DELETE CASCADE,
    CONSTRAINT uq_post_reports_post_id UNIQUE (post_id, id),
    CONSTRAINT uq_post_reports_reporter UNIQUE (post_id, reporter_id),
    CONSTRAINT chk_post_reports_status CHECK (
        status IN ('PENDING', 'UNDER_REVIEW', 'RESOLVED', 'REJECTED')
    ),
    CONSTRAINT chk_post_reports_reason CHECK (
        reason_code IN (
            'SPAM', 'HARASSMENT', 'HATE_SPEECH', 'VIOLENCE',
            'SEXUAL_CONTENT', 'MISINFORMATION', 'COPYRIGHT', 'OTHER'
        )
    ),
    CONSTRAINT chk_post_reports_reason_detail CHECK (
        reason_detail IS NULL OR length(trim(reason_detail)) > 0
    ),
    CONSTRAINT chk_post_reports_resolution CHECK (
        (
            status IN ('PENDING', 'UNDER_REVIEW')
            AND resolved_by IS NULL
            AND resolved_at IS NULL
            AND resolution_note IS NULL
        )
        OR (
            status IN ('RESOLVED', 'REJECTED')
            AND resolved_by IS NOT NULL
            AND resolved_at IS NOT NULL
        )
    )
);

CREATE TABLE post_histories (
    id UUID PRIMARY KEY,
    post_id UUID NOT NULL,
    post_author_user_id UUID NOT NULL,
    actor_user_id UUID,
    actor_type VARCHAR(20) NOT NULL,
    action VARCHAR(40) NOT NULL,
    previous_status VARCHAR(30),
    new_status VARCHAR(30),
    previous_visibility VARCHAR(20),
    new_visibility VARCHAR(20),
    reason_code VARCHAR(50),
    reason_detail TEXT,
    report_id UUID,
    snapshot_data JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_post_histories_post FOREIGN KEY (post_id)
        REFERENCES posts(id) ON DELETE RESTRICT,
    CONSTRAINT fk_post_histories_report FOREIGN KEY (post_id, report_id)
        REFERENCES post_reports(post_id, id) ON DELETE RESTRICT,
    CONSTRAINT chk_post_histories_actor_type CHECK (
        actor_type IN ('USER', 'ADMIN', 'SYSTEM')
    ),
    CONSTRAINT chk_post_histories_actor CHECK (
        (actor_type IN ('USER', 'ADMIN') AND actor_user_id IS NOT NULL)
        OR actor_type = 'SYSTEM'
    ),
    CONSTRAINT chk_post_histories_action CHECK (
        action IN (
            'CREATED', 'UPDATED', 'PUBLISHED', 'VISIBILITY_CHANGED',
            'HIDDEN', 'RESTORED', 'DELETED'
        )
    ),
    CONSTRAINT chk_post_histories_previous_status CHECK (
        previous_status IS NULL OR previous_status IN ('DRAFT', 'PUBLISHED', 'HIDDEN')
    ),
    CONSTRAINT chk_post_histories_new_status CHECK (
        new_status IS NULL OR new_status IN ('DRAFT', 'PUBLISHED', 'HIDDEN')
    ),
    CONSTRAINT chk_post_histories_previous_visibility CHECK (
        previous_visibility IS NULL OR previous_visibility IN ('PUBLIC', 'PRIVATE', 'FOLLOWERS')
    ),
    CONSTRAINT chk_post_histories_new_visibility CHECK (
        new_visibility IS NULL OR new_visibility IN ('PUBLIC', 'PRIVATE', 'FOLLOWERS')
    ),
    CONSTRAINT chk_post_histories_snapshot CHECK (
        jsonb_typeof(snapshot_data) = 'object'
    )
);

CREATE INDEX idx_post_types_active_order
    ON post_types(display_order, id)
    WHERE is_active = TRUE;

CREATE INDEX idx_post_topics_active_order
    ON post_topics(display_order, id)
    WHERE is_active = TRUE;

CREATE INDEX idx_posts_author
    ON posts(author_user_id, created_at DESC, id DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_posts_published
    ON posts(published_at DESC, id DESC)
    WHERE status = 'PUBLISHED'
      AND visibility = 'PUBLIC'
      AND deleted_at IS NULL;

CREATE INDEX idx_posts_status
    ON posts(status, created_at DESC, id DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_posts_type_published
    ON posts(post_type_id, published_at DESC, id DESC)
    WHERE status = 'PUBLISHED'
      AND visibility = 'PUBLIC'
      AND deleted_at IS NULL;

CREATE INDEX idx_posts_topic_published
    ON posts(topic_id, published_at DESC, id DESC)
    WHERE topic_id IS NOT NULL
      AND status = 'PUBLISHED'
      AND visibility = 'PUBLIC'
      AND deleted_at IS NULL;

CREATE INDEX idx_post_media_post_order
    ON post_media(post_id, display_order, id);

CREATE INDEX idx_post_crop_types_crop
    ON post_crop_types(crop_type_id, post_id);

CREATE INDEX idx_post_comments_post_created
    ON post_comments(post_id, created_at DESC, id DESC)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_post_comments_parent
    ON post_comments(parent_comment_id, created_at, id)
    WHERE parent_comment_id IS NOT NULL
      AND deleted_at IS NULL;

CREATE INDEX idx_post_reactions_post
    ON post_reactions(post_id, reaction_type);

CREATE INDEX idx_post_bookmarks_user_created
    ON post_bookmarks(user_id, created_at DESC, post_id DESC);

CREATE INDEX idx_post_shares_post
    ON post_shares(post_id);

CREATE INDEX idx_post_shares_user_created
    ON post_shares(shared_by_user_id, created_at DESC, id DESC);

CREATE INDEX idx_post_reports_post
    ON post_reports(post_id, created_at DESC, id DESC);

CREATE INDEX idx_post_reports_status
    ON post_reports(status, created_at, id)
    WHERE status IN ('PENDING', 'UNDER_REVIEW');

CREATE INDEX idx_post_histories_post_created
    ON post_histories(post_id, created_at DESC, id DESC);

CREATE INDEX idx_post_histories_author_created
    ON post_histories(post_author_user_id, created_at DESC, id DESC);

CREATE INDEX idx_post_histories_actor_created
    ON post_histories(actor_user_id, created_at DESC, id DESC)
    WHERE actor_user_id IS NOT NULL;

CREATE INDEX idx_post_histories_action_created
    ON post_histories(action, created_at DESC, id DESC);

CREATE INDEX idx_post_histories_created
    ON post_histories(created_at DESC, id DESC);
