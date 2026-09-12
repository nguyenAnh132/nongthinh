package com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.DeleteAiModelDiseaseMappingUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelDiseaseMappingRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteAiModelDiseaseMappingUseCaseImpl implements DeleteAiModelDiseaseMappingUseCase {

    private final AiModelDiseaseMappingUseCaseSupport support;
    private final AiModelDiseaseMappingRepository mappingRepository;

    @Override
    @Transactional
    public void execute(UUID id) {
        support.requireMapping(id);
        mappingRepository.deleteById(id);
    }
}
