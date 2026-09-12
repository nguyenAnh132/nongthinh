package com.nongthinh.post_service.application.port.in.post;

import com.nongthinh.post_service.application.view.PostStatisticsBucket;
import com.nongthinh.post_service.application.view.PostStatisticsView;
import java.time.Instant;
import java.time.ZoneId;

public interface GetPostStatisticsUseCase {
    PostStatisticsView execute(
            Instant from,
            Instant to,
            PostStatisticsBucket bucket,
            ZoneId zoneId
    );
}
