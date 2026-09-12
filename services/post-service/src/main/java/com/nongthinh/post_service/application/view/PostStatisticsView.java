package com.nongthinh.post_service.application.view;

import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record PostStatisticsView(
        long totalPosts,
        long postsInRange,
        Instant from,
        Instant to,
        PostStatisticsBucket bucket,
        String timeZone,
        Map<PostStatus, Long> statusCounts,
        List<PostTimelinePointView> timeline
) {
    public PostStatisticsView {
        statusCounts = Map.copyOf(statusCounts);
        timeline = List.copyOf(timeline);
    }
}
