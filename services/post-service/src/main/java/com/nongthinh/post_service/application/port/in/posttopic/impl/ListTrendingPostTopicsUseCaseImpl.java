package com.nongthinh.post_service.application.port.in.posttopic.impl;

import com.nongthinh.post_service.application.model.TrendingPostTopic;
import com.nongthinh.post_service.application.port.in.posttopic.ListTrendingPostTopicsUseCase;
import com.nongthinh.post_service.application.port.out.TrendingPostTopicQuery;
import com.nongthinh.post_service.application.view.TrendingPostTopicView;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListTrendingPostTopicsUseCaseImpl implements ListTrendingPostTopicsUseCase {
    private static final int MAX_LIMIT = 20;

    private final TrendingPostTopicQuery query;

    @Override
    @Transactional(readOnly = true)
    public List<TrendingPostTopicView> execute(int limit) {
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT);
        }

        List<TrendingPostTopic> topics = query.findTop(limit);
        return IntStream.range(0, topics.size())
                .mapToObj(index -> TrendingPostTopicView.from(index + 1, topics.get(index)))
                .toList();
    }
}
