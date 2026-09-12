package com.nongthinh.agri_catalog_service.application.port.in.croptype.impl;

import java.util.UUID;
import org.springframework.stereotype.Component;
import com.nongthinh.agri_catalog_service.application.port.out.repository.CropTypeRepository;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.croptype.CropType;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CropTypeUseCaseSupport {

    private final CropTypeRepository cropTypeRepository;

    public CropType requireCropType(UUID id) {
        return cropTypeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CROP_TYPE_NOT_FOUND));
    }

    public CropType requireActiveCropType(UUID id) {
        CropType cropType = requireCropType(id);
        if (!cropType.isActive()) {
            throw new BusinessException(ErrorCode.CROP_TYPE_INACTIVE);
        }
        return cropType;
    }
}

