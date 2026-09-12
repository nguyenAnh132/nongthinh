package com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.GetAiModelVersionUseCase;
import com.nongthinh.agri_catalog_service.application.view.AiModelVersionView;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetAiModelVersionUseCaseImpl implements GetAiModelVersionUseCase {

    private final AiModelVersionUseCaseSupport support;

    @Override
    @Transactional(readOnly = true)
    public AiModelVersionView execute(UUID versionId) {
        return AiModelVersionView.from(support.requireVersion(versionId));
    }
}
