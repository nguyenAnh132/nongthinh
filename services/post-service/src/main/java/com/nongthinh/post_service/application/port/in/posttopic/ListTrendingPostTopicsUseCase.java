package com.nongthinh.post_service.application.port.in.posttopic;

import com.nongthinh.post_service.application.view.TrendingPostTopicView;
import java.util.List;

public interface ListTrendingPostTopicsUseCase {
    List<TrendingPostTopicView> execute(int limit);
}
