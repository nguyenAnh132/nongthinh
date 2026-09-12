package com.nongthinh.post_service.application.model;

import java.util.UUID;

public record TrendingPostTopic(UUID id, String name, String slug, long postCount) {
}
