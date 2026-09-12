package com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment.DeactivateAiModelDeploymentUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.impl.AiModelVersionUseCaseSupport;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelDeploymentRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelVersionRepository;
import com.nongthinh.agri_catalog_service.application.view.AiModelDeploymentView;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.AiModelDeployment;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersion;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeactivateAiModelDeploymentUseCaseImpl implements DeactivateAiModelDeploymentUseCase {

    private final AiModelVersionUseCaseSupport support;
    private final AiModelDeploymentRepository aiModelDeploymentRepository;
    private final AiModelVersionRepository aiModelVersionRepository;

    @Override
    @Transactional
    public AiModelDeploymentView execute(UUID deploymentId) {
        AiModelDeployment deployment = aiModelDeploymentRepository.findById(deploymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AI_MODEL_DEPLOYMENT_NOT_FOUND));
        AiModelVersion version = support.requireVersion(deployment.getModelVersionId());
        deployment.deactivate();
        AiModelDeployment saved = aiModelDeploymentRepository.save(deployment);
        if (!aiModelDeploymentRepository.existsActiveByModelVersionId(version.getId())) {
            version.deactivate();
            aiModelVersionRepository.save(version);
        }
        return AiModelDeploymentView.from(saved);
    }
}
