package com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldiseasemapping.GetAiModelDiseaseMappingUseCase;
import com.nongthinh.agri_catalog_service.application.view.AiModelDiseaseMappingView;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetAiModelDiseaseMappingUseCaseImpl implements GetAiModelDiseaseMappingUseCase {

    private final AiModelDiseaseMappingUseCaseSupport support;

    @Override
    @Transactional(readOnly = true)
    public AiModelDiseaseMappingView execute(UUID id) {
        return AiModelDiseaseMappingView.from(support.requireMapping(id));
    }
}
