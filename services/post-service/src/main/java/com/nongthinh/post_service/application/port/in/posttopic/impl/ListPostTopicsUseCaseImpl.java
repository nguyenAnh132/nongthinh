package com.nongthinh.post_service.application.port.in.posttopic.impl;

import com.nongthinh.post_service.application.port.in.posttopic.ListPostTopicsUseCase;
import com.nongthinh.post_service.application.port.out.repository.PostTopicRepository;
import com.nongthinh.post_service.application.view.PostTopicView;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListPostTopicsUseCaseImpl implements ListPostTopicsUseCase {
    private final PostTopicRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<PostTopicView> execute(boolean activeOnly) {
        return (activeOnly ? repository.findAllActive() : repository.findAll())
                .stream().map(PostTopicView::from).toList();
    }
}
