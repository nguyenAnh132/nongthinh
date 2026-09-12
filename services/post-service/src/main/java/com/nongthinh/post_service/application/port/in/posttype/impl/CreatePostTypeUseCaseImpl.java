package com.nongthinh.post_service.application.port.in.posttype.impl;

import com.nongthinh.post_service.application.command.CreatePostTypeCommand;
import com.nongthinh.post_service.application.port.in.posttype.CreatePostTypeUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.IdGenerator;
import com.nongthinh.post_service.application.port.out.repository.PostTypeRepository;
import com.nongthinh.post_service.application.view.PostTypeView;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.post.PostType;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatePostTypeUseCaseImpl implements CreatePostTypeUseCase {
    private final PostTypeRepository repository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public PostTypeView execute(CreatePostTypeCommand command) {
        Objects.requireNonNull(command, "command is required");
        if (repository.findByCode(command.code()).isPresent()) {
            throw new BusinessException(ErrorCode.POST_TYPE_CODE_ALREADY_EXISTS);
        }
        PostType value = PostType.create(idGenerator.generate(), command.code(), command.name(),
                normalizeDescription(command.description()), command.displayOrder(), clockProvider.now());
        return PostTypeView.from(repository.save(value));
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank() ? null : description;
    }
}
