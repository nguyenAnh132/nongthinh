package com.nongthinh.agri_catalog_service.application.port.in.croptype.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.croptype.DeleteCropTypeUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.CropTypeRepository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.DiseaseRepository;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteCropTypeUseCaseImpl implements DeleteCropTypeUseCase {

    private final CropTypeUseCaseSupport support;
    private final CropTypeRepository cropTypeRepository;
    private final DiseaseRepository diseaseRepository;
    private final AiModelRepository aiModelRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public void execute(UUID id) {
        var cropType = support.requireCropType(id);
        if (diseaseRepository.existsByCropTypeId(id) || aiModelRepository.existsByCropTypeId(id)) {
            throw new BusinessException(ErrorCode.CROP_TYPE_IN_USE);
        }

        cropType.delete(
                currentUserProvider.getCurrentUser().getUserId(),
                clockProvider.now()
        );
        cropTypeRepository.save(cropType);
    }
}
