package com.nongthinh.post_service.application.port.in.posttopic.impl;

import com.nongthinh.post_service.application.command.UpdatePostTopicCommand;
import com.nongthinh.post_service.application.port.in.posttopic.UpdatePostTopicUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.repository.PostTopicRepository;
import com.nongthinh.post_service.application.view.PostTopicView;
import com.nongthinh.post_service.domain.post.PostTopic;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePostTopicUseCaseImpl implements UpdatePostTopicUseCase {
    private final PostTopicUseCaseSupport support;
    private final PostTopicRepository repository;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public PostTopicView execute(UUID id, UpdatePostTopicCommand command) {
        Objects.requireNonNull(command, "command is required");
        PostTopic value = support.require(id);
        Instant now = clockProvider.now();
        value.update(command.name(), normalizeDescription(command.description()), command.displayOrder(), now);
        if (value.isActive() != command.active()) {
            if (command.active()) value.activate(now); else value.deactivate(now);
        }
        return PostTopicView.from(repository.save(value));
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank() ? null : description;
    }
}
