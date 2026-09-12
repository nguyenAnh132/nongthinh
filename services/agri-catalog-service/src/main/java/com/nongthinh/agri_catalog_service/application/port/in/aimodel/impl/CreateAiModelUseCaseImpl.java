package com.nongthinh.agri_catalog_service.application.port.in.aimodel.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.command.AiModelCreationCommand;
import com.nongthinh.agri_catalog_service.application.port.in.aimodel.CreateAiModelUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.IdGenerator;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelRepository;
import com.nongthinh.agri_catalog_service.application.view.AiModelView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodel.AiModel;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateAiModelUseCaseImpl implements CreateAiModelUseCase {

    private final AiModelUseCaseSupport support;
    private final AiModelRepository aiModelRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public AiModelView execute(AiModelCreationCommand command) {
        Objects.requireNonNull(command, "command is required");
        if (aiModelRepository.existsByCode(command.code())) {
            throw new BusinessException(ErrorCode.AI_MODEL_CODE_ALREADY_EXISTS);
        }

        support.requireActiveCropTypes(command.cropTypeIds());
        UUID actorId = currentUserProvider.getCurrentUser().getUserId();
        Instant now = clockProvider.now();
        AiModel aiModel = AiModel.create(
                idGenerator.generate(),
                command.code(),
                command.name(),
                command.description(),
                command.taskType(),
                command.cropCoverageType(),
                command.cropTypeIds(),
                actorId,
                now
        );
        return AiModelView.from(aiModelRepository.save(aiModel));
    }
}
