package com.nongthinh.agri_catalog_service.application.port.in.aimodel.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.aimodel.GetAiModelByIdUseCase;
import com.nongthinh.agri_catalog_service.application.view.AiModelView;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetAiModelByIdUseCaseImpl implements GetAiModelByIdUseCase {

    private final AiModelUseCaseSupport support;

    @Override
    @Transactional(readOnly = true)
    public AiModelView execute(UUID id) {
        return AiModelView.from(support.requireAiModel(id));
    }
}
