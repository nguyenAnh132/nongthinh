package com.nongthinh.post_service.application.view;

import com.nongthinh.post_service.application.model.TrendingPostTopic;
import java.util.UUID;

public record TrendingPostTopicView(int rank, UUID id, String name, String slug, long postCount) {
    public static TrendingPostTopicView from(int rank, TrendingPostTopic topic) {
        return new TrendingPostTopicView(
                rank, topic.id(), topic.name(), topic.slug(), topic.postCount());
    }
}
