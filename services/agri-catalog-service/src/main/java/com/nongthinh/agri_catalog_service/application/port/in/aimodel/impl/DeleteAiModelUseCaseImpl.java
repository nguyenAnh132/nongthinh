package com.nongthinh.agri_catalog_service.application.port.in.aimodel.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.aimodel.DeleteAiModelUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.ClockProvider;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelRepository;
import com.nongthinh.agri_catalog_service.common.currentuser.CurrentUserProvider;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteAiModelUseCaseImpl implements DeleteAiModelUseCase {

    private final AiModelUseCaseSupport support;
    private final AiModelRepository aiModelRepository;
    private final ClockProvider clockProvider;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public void execute(UUID id) {
        var aiModel = support.requireAiModel(id);
        aiModel.retire(currentUserProvider.getCurrentUser().getUserId(), clockProvider.now());
        aiModelRepository.save(aiModel);
    }
}
