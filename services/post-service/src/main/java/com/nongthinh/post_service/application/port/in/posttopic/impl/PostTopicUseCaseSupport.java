package com.nongthinh.post_service.application.port.in.posttopic.impl;

import com.nongthinh.post_service.application.port.out.repository.PostTopicRepository;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.post.PostTopic;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class PostTopicUseCaseSupport {
    private final PostTopicRepository repository;

    PostTopic require(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_TOPIC_NOT_FOUND));
    }
}
