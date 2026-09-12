package com.nongthinh.post_service.application.port.in.posttopic.impl;

import com.nongthinh.post_service.application.command.CreatePostTopicCommand;
import com.nongthinh.post_service.application.port.in.posttopic.CreatePostTopicUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.IdGenerator;
import com.nongthinh.post_service.application.port.out.repository.PostTopicRepository;
import com.nongthinh.post_service.application.view.PostTopicView;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.post.PostTopic;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatePostTopicUseCaseImpl implements CreatePostTopicUseCase {
    private final PostTopicRepository repository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public PostTopicView execute(CreatePostTopicCommand command) {
        Objects.requireNonNull(command, "command is required");
        if (repository.findBySlug(command.slug()).isPresent()) {
            throw new BusinessException(ErrorCode.POST_TOPIC_SLUG_ALREADY_EXISTS);
        }
        PostTopic value = PostTopic.create(idGenerator.generate(), command.name(), command.slug(),
                normalizeDescription(command.description()), command.displayOrder(), clockProvider.now());
        return PostTopicView.from(repository.save(value));
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank() ? null : description;
    }
}
