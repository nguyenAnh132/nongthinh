package com.nongthinh.post_service.application.model;

import java.time.Instant;
import java.util.UUID;

public record PostFeedCursor(Instant snapshotAt, Instant publishedAt, UUID postId, String scope) { }
