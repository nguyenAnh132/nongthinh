package com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.impl;

import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.command.AiModelDiseaseMappingCommand;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.UpdateAiModelDiseaseMappingUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelDiseaseMappingRepository;
import com.nongthinh.agri_catalog_service.application.view.AiModelDiseaseMappingView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodeldiseasemapping.AiModelDiseaseMapping;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateAiModelDiseaseMappingUseCaseImpl implements UpdateAiModelDiseaseMappingUseCase {

    private final AiModelDiseaseMappingUseCaseSupport support;
    private final AiModelDiseaseMappingRepository mappingRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public AiModelDiseaseMappingView execute(UUID id, AiModelDiseaseMappingCommand command) {
        AiModelDiseaseMapping mapping = support.requireMapping(id);
        support.validate(command);
        if (mappingRepository.existsByModelVersionClassIdAndCropTypeIdAndIdNot(
                command.modelVersionClassId(), command.cropTypeId(), id)) {
            throw new BusinessException(ErrorCode.AI_MODEL_DISEASE_MAPPING_ALREADY_EXISTS);
        }
        mapping.update(
                command.modelVersionClassId(),
                command.cropTypeId(),
                command.diseaseId(),
                currentUserProvider.getCurrentUser().getUserId(),
                clockProvider.now());
        try {
            return AiModelDiseaseMappingView.from(mappingRepository.save(mapping));
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(ErrorCode.AI_MODEL_DISEASE_MAPPING_ALREADY_EXISTS, ex);
        }
    }
}
