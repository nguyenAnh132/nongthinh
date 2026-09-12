package com.nongthinh.post_service.infra.persistence.adapter;

import com.nongthinh.post_service.application.model.TrendingPostTopic;
import com.nongthinh.post_service.application.port.out.TrendingPostTopicQuery;
import com.nongthinh.post_service.infra.persistence.repository.JpaPostTopicRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class TrendingPostTopicQueryImpl implements TrendingPostTopicQuery {
    private final JpaPostTopicRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<TrendingPostTopic> findTop(int limit) {
        return List.copyOf(repository.findTrending(PageRequest.of(0, limit)));
    }
}
