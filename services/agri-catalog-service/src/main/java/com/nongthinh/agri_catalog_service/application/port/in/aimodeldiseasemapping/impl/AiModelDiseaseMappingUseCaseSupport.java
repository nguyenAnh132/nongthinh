package com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.impl;

import java.util.UUID;
import org.springframework.stereotype.Component;
import com.nongthinh.agri_catalog_service.application.command.AiModelDiseaseMappingCommand;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelDiseaseMappingRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelVersionRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.CropTypeRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseRepository;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodeldiseasemapping.AiModelDiseaseMapping;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelClassKind;
import com.nongthinh.agri_catalog_service.domain.disease.Disease;
import com.nongthinh.agri_catalog_service.domain.disease.valueobject.ReviewStatus;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class AiModelDiseaseMappingUseCaseSupport {

    private final AiModelDiseaseMappingRepository mappingRepository;
    private final AiModelVersionRepository versionRepository;
    private final CropTypeRepository cropTypeRepository;
    private final DiseaseRepository diseaseRepository;

    AiModelDiseaseMapping requireMapping(UUID id) {
        return mappingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.AI_MODEL_DISEASE_MAPPING_NOT_FOUND));
    }

    void requireVersion(UUID id) {
        versionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.AI_MODEL_VERSION_NOT_FOUND));
    }

    void validate(AiModelDiseaseMappingCommand command) {
        var modelClass = versionRepository.findClassById(command.modelVersionClassId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AI_MODEL_DISEASE_MAPPING_CLASS_INVALID));
        if (modelClass.getClassKind() != AiModelClassKind.DISEASE) {
            throw new BusinessException(ErrorCode.AI_MODEL_DISEASE_MAPPING_CLASS_INVALID);
        }

        var cropType = cropTypeRepository.findById(command.cropTypeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CROP_TYPE_NOT_FOUND));
        if (!cropType.isActive()) {
            throw new BusinessException(ErrorCode.CROP_TYPE_INACTIVE);
        }

        Disease disease = diseaseRepository.findById(command.diseaseId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DISEASE_NOT_FOUND));
        if (disease.getReviewStatus() != ReviewStatus.APPROVED
                || !command.cropTypeId().equals(disease.getCropTypeId())) {
            throw new BusinessException(ErrorCode.AI_MODEL_DISEASE_MAPPING_DISEASE_INVALID);
        }
    }
}
