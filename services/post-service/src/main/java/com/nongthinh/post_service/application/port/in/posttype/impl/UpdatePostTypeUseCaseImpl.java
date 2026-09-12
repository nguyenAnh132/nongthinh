package com.nongthinh.post_service.application.port.in.posttype.impl;

import com.nongthinh.post_service.application.command.UpdatePostTypeCommand;
import com.nongthinh.post_service.application.port.in.posttype.UpdatePostTypeUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.repository.PostTypeRepository;
import com.nongthinh.post_service.application.view.PostTypeView;
import com.nongthinh.post_service.domain.post.PostType;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePostTypeUseCaseImpl implements UpdatePostTypeUseCase {
    private final PostTypeUseCaseSupport support;
    private final PostTypeRepository repository;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public PostTypeView execute(UUID id, UpdatePostTypeCommand command) {
        Objects.requireNonNull(command, "command is required");
        PostType value = support.require(id);
        Instant now = clockProvider.now();
        value.update(command.name(), normalizeDescription(command.description()), command.displayOrder(), now);
        if (value.isActive() != command.active()) {
            if (command.active()) value.activate(now); else value.deactivate(now);
        }
        return PostTypeView.from(repository.save(value));
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank() ? null : description;
    }
}
