package com.nongthinh.agri_catalog_service.application.port.in.aimodel.impl;

import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.command.AiModelUpdateCommand;
import com.nongthinh.agri_catalog_service.application.port.in.aimodel.UpdateAiModelUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelVersionRepository;
import com.nongthinh.agri_catalog_service.application.view.AiModelView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateAiModelUseCaseImpl implements UpdateAiModelUseCase {

    private final AiModelUseCaseSupport support;
    private final AiModelRepository aiModelRepository;
    private final AiModelVersionRepository aiModelVersionRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public AiModelView execute(UUID id, AiModelUpdateCommand command) {
        Objects.requireNonNull(command, "command is required");
        var aiModel = support.requireAiModel(id);
        if (aiModelVersionRepository.existsByModelId(id)
                && (aiModel.getCropCoverageType() != command.cropCoverageType()
                || !aiModel.getCropTypeIds().equals(command.cropTypeIds()))) {
            throw new BusinessException(ErrorCode.AI_MODEL_CROP_COVERAGE_IMMUTABLE);
        }
        support.requireActiveCropTypes(command.cropTypeIds());
        aiModel.update(
                command.name(),
                command.description(),
                command.cropCoverageType(),
                command.cropTypeIds(),
                currentUserProvider.getCurrentUser().getUserId(),
                clockProvider.now()
        );
        return AiModelView.from(aiModelRepository.save(aiModel));
    }
}
