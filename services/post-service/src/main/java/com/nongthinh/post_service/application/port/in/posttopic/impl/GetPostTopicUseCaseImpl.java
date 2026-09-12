package com.nongthinh.post_service.application.port.in.posttopic.impl;

import com.nongthinh.post_service.application.port.in.posttopic.GetPostTopicUseCase;
import com.nongthinh.post_service.application.view.PostTopicView;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetPostTopicUseCaseImpl implements GetPostTopicUseCase {
    private final PostTopicUseCaseSupport support;

    @Override
    @Transactional(readOnly = true)
    public PostTopicView execute(UUID id) {
        return PostTopicView.from(support.require(id));
    }
}
