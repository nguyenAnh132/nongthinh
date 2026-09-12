package com.nongthinh.agri_catalog_service.application.port.in.aimodel.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.aimodel.ListAiModelsUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelRepository;
import com.nongthinh.agri_catalog_service.application.view.AiModelView;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListAiModelsUseCaseImpl implements ListAiModelsUseCase {

    private final AiModelRepository aiModelRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AiModelView> execute() {
        return aiModelRepository.findAll().stream()
                .map(AiModelView::from)
                .toList();
    }
}
