package com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.impl;

import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.command.AiModelDiseaseMappingCommand;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.CreateAiModelDiseaseMappingUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.IdGenerator;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelDiseaseMappingRepository;
import com.nongthinh.agri_catalog_service.application.view.AiModelDiseaseMappingView;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodeldiseasemapping.AiModelDiseaseMapping;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateAiModelDiseaseMappingUseCaseImpl implements CreateAiModelDiseaseMappingUseCase {

    private final AiModelDiseaseMappingUseCaseSupport support;
    private final AiModelDiseaseMappingRepository mappingRepository;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public AiModelDiseaseMappingView execute(AiModelDiseaseMappingCommand command) {
        support.validate(command);
        if (mappingRepository.existsByModelVersionClassIdAndCropTypeId(
                command.modelVersionClassId(), command.cropTypeId())) {
            throw new BusinessException(ErrorCode.AI_MODEL_DISEASE_MAPPING_ALREADY_EXISTS);
        }
        Instant now = clockProvider.now();
        AiModelDiseaseMapping mapping = AiModelDiseaseMapping.create(
                idGenerator.generate(),
                command.modelVersionClassId(),
                command.cropTypeId(),
                command.diseaseId(),
                currentUserProvider.getCurrentUser().getUserId(),
                now);
        try {
            return AiModelDiseaseMappingView.from(mappingRepository.save(mapping));
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(ErrorCode.AI_MODEL_DISEASE_MAPPING_ALREADY_EXISTS, ex);
        }
    }
}
