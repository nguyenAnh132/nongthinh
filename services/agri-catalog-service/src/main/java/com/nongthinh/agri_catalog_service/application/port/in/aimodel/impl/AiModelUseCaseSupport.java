package com.nongthinh.agri_catalog_service.application.port.in.aimodel.impl;

import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.CropTypeRepository;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodel.AiModel;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class AiModelUseCaseSupport {

    private final AiModelRepository aiModelRepository;
    private final CropTypeRepository cropTypeRepository;

    AiModel requireAiModel(UUID id) {
        return aiModelRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.AI_MODEL_NOT_FOUND));
    }

    void requireActiveCropTypes(Set<UUID> cropTypeIds) {
        if (cropTypeIds == null) {
            return;
        }
        for (UUID cropTypeId : cropTypeIds) {
            var cropType = cropTypeRepository.findById(cropTypeId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CROP_TYPE_NOT_FOUND));
            if (!cropType.isActive()) {
                throw new BusinessException(ErrorCode.CROP_TYPE_INACTIVE);
            }
        }
    }
}
