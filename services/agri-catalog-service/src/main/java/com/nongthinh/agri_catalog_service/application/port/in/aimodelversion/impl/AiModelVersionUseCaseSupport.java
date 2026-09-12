package com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.impl;

import java.util.UUID;
import org.springframework.stereotype.Component;
import com.nongthinh.agri_catalog_service.application.model.FileMetadata;
import com.nongthinh.agri_catalog_service.application.port.out.FileServicePort;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelVersionRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.CropTypeRepository;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodel.AiModel;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.CropCoverageType;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.AiModelDeployment;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.valueobject.AiModelDeploymentScope;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersion;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelVersionStatus;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AiModelVersionUseCaseSupport {

    private final AiModelRepository aiModelRepository;
    private final AiModelVersionRepository aiModelVersionRepository;
    private final CropTypeRepository cropTypeRepository;
    private final FileServicePort fileServicePort;

    public AiModel requireModel(UUID id) {
        return aiModelRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.AI_MODEL_NOT_FOUND));
    }

    public AiModelVersion requireVersion(UUID id) {
        return aiModelVersionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.AI_MODEL_VERSION_NOT_FOUND));
    }

    public void requireModelCanAcceptVersion(AiModel model) {
        if (model.getStatus().name().equals("RETIRED")) {
            throw new BusinessException(ErrorCode.AI_MODEL_RETIRED);
        }
    }

    public void requireActivePrivateArtifact(UUID artifactFileId) {
        FileMetadata file = fileServicePort.getActiveFile(artifactFileId);
        if (!file.isActiveModelArtifact()) {
            throw new BusinessException(ErrorCode.AI_MODEL_VERSION_ARTIFACT_INVALID);
        }
    }

    public void requireReadyForDeployment(AiModelVersion version) {
        if (version.getStatus() != AiModelVersionStatus.READY
                && version.getStatus() != AiModelVersionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.AI_MODEL_VERSION_STATE_INVALID);
        }
    }

    public void requireDeploymentCoverage(AiModel model, AiModelDeployment deployment) {
        if (deployment.getDeploymentScope() == AiModelDeploymentScope.ALL_CROPS) {
            if (model.getCropCoverageType() != CropCoverageType.ALL_CROPS) {
                throw new BusinessException(ErrorCode.AI_MODEL_DEPLOYMENT_CROP_COVERAGE_INVALID);
            }
            return;
        }

        UUID cropTypeId = deployment.getCropTypeId();
        if (model.getCropCoverageType() != CropCoverageType.SELECTED_CROPS
                || !model.getCropTypeIds().contains(cropTypeId)) {
            throw new BusinessException(ErrorCode.AI_MODEL_DEPLOYMENT_CROP_COVERAGE_INVALID);
        }
        var cropType = cropTypeRepository.findById(cropTypeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CROP_TYPE_NOT_FOUND));
        if (!cropType.isActive()) {
            throw new BusinessException(ErrorCode.CROP_TYPE_INACTIVE);
        }
    }
}
