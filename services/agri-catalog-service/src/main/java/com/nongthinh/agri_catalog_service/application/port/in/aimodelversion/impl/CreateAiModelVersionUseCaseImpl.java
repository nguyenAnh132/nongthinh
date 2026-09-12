package com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.impl;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.command.AiModelVersionCreationCommand;
import com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.CreateAiModelVersionUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.IdGenerator;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelVersionRepository;
import com.nongthinh.agri_catalog_service.application.view.AiModelVersionView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodel.AiModel;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersion;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersionClass;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateAiModelVersionUseCaseImpl implements CreateAiModelVersionUseCase {

    private final AiModelVersionUseCaseSupport support;
    private final AiModelVersionRepository aiModelVersionRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public AiModelVersionView execute(UUID modelId, AiModelVersionCreationCommand command) {
        Objects.requireNonNull(command, "command is required");
        if (command.classes() == null || command.classes().isEmpty()) {
            throw new BusinessException(ErrorCode.AI_MODEL_VERSION_MANIFEST_INVALID);
        }
        AiModel model = support.requireModel(modelId);
        support.requireModelCanAcceptVersion(model);
        if (aiModelVersionRepository.existsByModelIdAndVersion(modelId, command.version())) {
            throw new BusinessException(ErrorCode.AI_MODEL_VERSION_ALREADY_EXISTS);
        }
        support.requireActivePrivateArtifact(command.artifactFileId());

        UUID actorId = currentUserProvider.getCurrentUser().getUserId();
        Instant now = clockProvider.now();
        UUID versionId = idGenerator.generate();
        List<AiModelVersionClass> manifest = command.classes().stream()
                .map(item -> AiModelVersionClass.create(
                        idGenerator.generate(), versionId, item.classIndex(), item.classCode(),
                        item.displayName(), item.classKind()))
                .toList();
        AiModelVersion version = AiModelVersion.create(
                versionId,
                modelId,
                command.version(),
                command.artifactFileId(),
                command.artifactSha256(),
                command.inputWidth(),
                command.inputHeight(),
                manifest,
                actorId,
                now
        );
        return AiModelVersionView.from(aiModelVersionRepository.save(version));
    }
}
