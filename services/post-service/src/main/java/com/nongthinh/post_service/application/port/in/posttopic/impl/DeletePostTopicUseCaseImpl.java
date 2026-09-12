package com.nongthinh.post_service.application.port.in.posttopic.impl;

import com.nongthinh.post_service.application.port.in.posttopic.DeletePostTopicUseCase;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.port.out.repository.PostTopicRepository;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeletePostTopicUseCaseImpl implements DeletePostTopicUseCase {
    private final PostTopicUseCaseSupport support;
    private final PostTopicRepository repository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public void execute(UUID id) {
        support.require(id);
        if (postRepository.existsByTopicId(id)) {
            throw new BusinessException(ErrorCode.POST_TOPIC_IN_USE);
        }
        repository.deleteById(id);
    }
}
