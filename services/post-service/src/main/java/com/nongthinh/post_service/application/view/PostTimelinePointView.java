package com.nongthinh.post_service.application.view;

import java.time.Instant;

public record PostTimelinePointView(
        Instant bucketStart,
        Instant bucketEnd,
        long count
) {
}
