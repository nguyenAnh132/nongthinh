package com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.MarkAiModelVersionReadyUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.AiModelVersionMappingReadiness;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelVersionRepository;
import com.nongthinh.agri_catalog_service.application.view.AiModelVersionView;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersion;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MarkAiModelVersionReadyUseCaseImpl implements MarkAiModelVersionReadyUseCase {

    private final AiModelVersionUseCaseSupport support;
    private final AiModelVersionRepository aiModelVersionRepository;
    private final AiModelVersionMappingReadiness aiModelVersionMappingReadiness;

    @Override
    @Transactional
    public AiModelVersionView execute(UUID versionId) {
        AiModelVersion version = support.requireVersion(versionId);
        support.requireModelCanAcceptVersion(support.requireModel(version.getModelId()));
        support.requireActivePrivateArtifact(version.getArtifactFileId());
        if (!aiModelVersionMappingReadiness.hasCompleteDiseaseMappings(version)) {
            throw new BusinessException(ErrorCode.AI_MODEL_VERSION_MAPPING_NOT_READY);
        }
        version.markReady();
        return AiModelVersionView.from(aiModelVersionRepository.save(version));
    }
}
