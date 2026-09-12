package com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.impl;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.ListAiModelDiseaseMappingsUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelDiseaseMappingRepository;
import com.nongthinh.agri_catalog_service.application.view.AiModelDiseaseMappingView;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListAiModelDiseaseMappingsUseCaseImpl implements ListAiModelDiseaseMappingsUseCase {

    private final AiModelDiseaseMappingUseCaseSupport support;
    private final AiModelDiseaseMappingRepository mappingRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AiModelDiseaseMappingView> execute(UUID modelVersionId) {
        support.requireVersion(modelVersionId);
        return mappingRepository.findAllByModelVersionId(modelVersionId).stream()
                .map(AiModelDiseaseMappingView::from)
                .toList();
    }
}
