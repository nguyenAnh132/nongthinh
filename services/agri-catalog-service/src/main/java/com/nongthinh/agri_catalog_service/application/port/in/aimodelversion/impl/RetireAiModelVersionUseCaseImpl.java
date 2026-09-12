package com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.impl;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.RetireAiModelVersionUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelDeploymentRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelVersionRepository;
import com.nongthinh.agri_catalog_service.application.view.AiModelVersionView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersion;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RetireAiModelVersionUseCaseImpl implements RetireAiModelVersionUseCase {

    private final AiModelVersionUseCaseSupport support;
    private final AiModelVersionRepository aiModelVersionRepository;
    private final AiModelDeploymentRepository aiModelDeploymentRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public AiModelVersionView execute(UUID versionId) {
        AiModelVersion version = support.requireVersion(versionId);
        if (aiModelDeploymentRepository.existsActiveByModelVersionId(versionId)) {
            throw new BusinessException(ErrorCode.AI_MODEL_VERSION_HAS_ACTIVE_DEPLOYMENT);
        }
        Instant now = clockProvider.now();
        version.retire(currentUserProvider.getCurrentUser().getUserId(), now);
        return AiModelVersionView.from(aiModelVersionRepository.save(version));
    }
}
