package com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.impl;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.aimodelversion.ListAiModelVersionsUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelVersionRepository;
import com.nongthinh.agri_catalog_service.application.view.AiModelVersionView;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListAiModelVersionsUseCaseImpl implements ListAiModelVersionsUseCase {

    private final AiModelVersionUseCaseSupport support;
    private final AiModelVersionRepository aiModelVersionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AiModelVersionView> execute(UUID modelId) {
        support.requireModel(modelId);
        return aiModelVersionRepository.findAllByModelId(modelId).stream()
                .map(AiModelVersionView::from)
                .toList();
    }
}
