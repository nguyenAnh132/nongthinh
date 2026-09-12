package com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment.impl;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment.ActivateAiModelDeploymentUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.impl.AiModelVersionUseCaseSupport;
import com.nongthinh.agri_catalog_service.application.model.AiModelRuntimeWarmupRequest;
import com.nongthinh.agri_catalog_service.application.port.out.AiModelRuntimeWarmer;
import com.nongthinh.agri_catalog_service.application.port.out.AiModelVersionMappingReadiness;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelDeploymentRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelVersionRepository;
import com.nongthinh.agri_catalog_service.application.view.AiModelDeploymentView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodel.AiModel;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.AiModelDeployment;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersion;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivateAiModelDeploymentUseCaseImpl implements ActivateAiModelDeploymentUseCase {

    private final AiModelVersionUseCaseSupport support;
    private final AiModelDeploymentRepository aiModelDeploymentRepository;
    private final AiModelVersionRepository aiModelVersionRepository;
    private final AiModelRuntimeWarmer aiModelRuntimeWarmer;
    private final AiModelVersionMappingReadiness aiModelVersionMappingReadiness;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public AiModelDeploymentView execute(UUID deploymentId) {
        AiModelDeployment deployment = aiModelDeploymentRepository.findById(deploymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AI_MODEL_DEPLOYMENT_NOT_FOUND));
        AiModelVersion version = support.requireVersion(deployment.getModelVersionId());
        AiModel model = support.requireModel(version.getModelId());
        support.requireModelCanAcceptVersion(model);
        support.requireReadyForDeployment(version);
        if (!aiModelVersionMappingReadiness.hasCompleteDiseaseMappings(version)) {
            throw new BusinessException(ErrorCode.AI_MODEL_VERSION_MAPPING_NOT_READY);
        }
        support.requireDeploymentCoverage(model, deployment);
        support.requireActivePrivateArtifact(version.getArtifactFileId());

        aiModelRuntimeWarmer.warm(new AiModelRuntimeWarmupRequest(
                version.getId(),
                version.getArtifactFileId(),
                version.getArtifactSha256(),
                version.getInputWidth(),
                version.getInputHeight()));

        Instant now = clockProvider.now();
        UUID actorId = currentUserProvider.getCurrentUser().getUserId();
        aiModelDeploymentRepository.findActiveWithScopeCropAndPriority(
                        deployment.getDeploymentScope(),
                        deployment.getCropTypeId(),
                        deployment.getPriority(),
                        deployment.getId())
                .ifPresent(activeDeployment -> replaceActiveDeployment(activeDeployment));
        deployment.activate(actorId, now);
        version.activate();
        aiModelVersionRepository.save(version);
        try {
            return AiModelDeploymentView.from(aiModelDeploymentRepository.saveAndFlush(deployment));
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(ErrorCode.AI_MODEL_DEPLOYMENT_PRIORITY_CONFLICT, ex);
        }
    }

    private void replaceActiveDeployment(AiModelDeployment activeDeployment) {
        activeDeployment.deactivate();
        aiModelDeploymentRepository.saveAndFlush(activeDeployment);

        AiModelVersion activeVersion = support.requireVersion(activeDeployment.getModelVersionId());
        if (!aiModelDeploymentRepository.existsActiveByModelVersionId(activeVersion.getId())) {
            activeVersion.deactivate();
            aiModelVersionRepository.save(activeVersion);
        }
    }
}
