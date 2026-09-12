package com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment.impl;

import java.time.Instant;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.command.AiModelDeploymentCreationCommand;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment.CreateAiModelDeploymentUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.impl.AiModelVersionUseCaseSupport;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.IdGenerator;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelDeploymentRepository;
import com.nongthinh.agri_catalog_service.application.view.AiModelDeploymentView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.AiModelDeployment;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersion;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateAiModelDeploymentUseCaseImpl implements CreateAiModelDeploymentUseCase {

    private final AiModelVersionUseCaseSupport support;
    private final AiModelDeploymentRepository aiModelDeploymentRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public AiModelDeploymentView execute(AiModelDeploymentCreationCommand command) {
        Objects.requireNonNull(command, "command is required");
        AiModelVersion version = support.requireVersion(command.modelVersionId());
        support.requireReadyForDeployment(version);
        AiModelDeployment deployment = AiModelDeployment.create(
                idGenerator.generate(),
                version.getId(),
                command.cropTypeId(),
                command.deploymentScope(),
                command.priority(),
                currentUserProvider.getCurrentUser().getUserId(),
                clockProvider.now()
        );
        support.requireModelCanAcceptVersion(support.requireModel(version.getModelId()));
        support.requireDeploymentCoverage(support.requireModel(version.getModelId()), deployment);
        return AiModelDeploymentView.from(aiModelDeploymentRepository.save(deployment));
    }
}
