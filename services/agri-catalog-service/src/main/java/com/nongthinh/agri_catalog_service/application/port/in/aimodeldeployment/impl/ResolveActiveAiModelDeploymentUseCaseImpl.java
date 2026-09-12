package com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment.impl;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment.ResolveActiveAiModelDeploymentUseCase;
import com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.impl.AiModelVersionUseCaseSupport;
import com.nongthinh.agri_catalog_service.application.port.out.AiModelVersionMappingReadiness;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelDeploymentRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.CropTypeRepository;
import com.nongthinh.agri_catalog_service.application.view.AiModelDeploymentResolutionView;
import com.nongthinh.agri_catalog_service.application.view.AiModelVersionClassView;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodel.AiModel;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.AiModelStatus;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.AiModelDeployment;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersion;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelVersionStatus;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResolveActiveAiModelDeploymentUseCaseImpl implements ResolveActiveAiModelDeploymentUseCase {

    private final AiModelVersionUseCaseSupport support;
    private final AiModelDeploymentRepository aiModelDeploymentRepository;
    private final CropTypeRepository cropTypeRepository;
    private final AiModelVersionMappingReadiness aiModelVersionMappingReadiness;

    @Override
    @Transactional(readOnly = true)
    public AiModelDeploymentResolutionView execute(UUID cropTypeId) {
        var cropType = cropTypeRepository.findById(cropTypeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CROP_TYPE_NOT_FOUND));
        if (!cropType.isActive()) {
            throw new BusinessException(ErrorCode.CROP_TYPE_INACTIVE);
        }

        AiModelDeploymentResolutionView cropSpecific = resolve(
                aiModelDeploymentRepository.findActiveForCrop(cropTypeId), cropTypeId);
        return cropSpecific != null
                ? cropSpecific
                : requireResolution(aiModelDeploymentRepository.findActiveForAllCrops(), cropTypeId);
    }

    private AiModelDeploymentResolutionView requireResolution(
            List<AiModelDeployment> deployments, UUID cropTypeId) {
        AiModelDeploymentResolutionView resolution = resolve(deployments, cropTypeId);
        if (resolution == null) {
            throw new BusinessException(ErrorCode.MODEL_NOT_AVAILABLE_FOR_CROP);
        }
        return resolution;
    }

    private AiModelDeploymentResolutionView resolve(List<AiModelDeployment> deployments, UUID cropTypeId) {
        for (AiModelDeployment deployment : deployments) {
            AiModelVersion version = support.requireVersion(deployment.getModelVersionId());
            AiModel model = support.requireModel(version.getModelId());
            if (version.getStatus() != AiModelVersionStatus.ACTIVE || model.getStatus() == AiModelStatus.RETIRED) {
                continue;
            }
            if (!aiModelVersionMappingReadiness.hasCompleteDiseaseMappingsForCrop(version, cropTypeId)) {
                throw new BusinessException(ErrorCode.CATALOG_MAPPING_NOT_READY);
            }
            return new AiModelDeploymentResolutionView(
                    deployment.getId(),
                    model.getId(),
                    model.getCode(),
                    model.getName(),
                    version.getId(),
                    version.getVersion(),
                    version.getArtifactFileId(),
                    version.getArtifactSha256(),
                    version.getInputWidth(),
                    version.getInputHeight(),
                    version.getClasses().stream().map(AiModelVersionClassView::from).toList()
            );
        }
        return null;
    }
}
