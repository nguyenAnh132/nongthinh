package com.nongthinh.post_service.application.port.out;

import com.nongthinh.post_service.application.model.TrendingPostTopic;
import java.util.List;

public interface TrendingPostTopicQuery {
    List<TrendingPostTopic> findTop(int limit);
}
