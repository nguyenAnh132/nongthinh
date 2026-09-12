package com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.in.aimodeldeployment.GetAiModelDeploymentsUseCase;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelDeploymentRepository;
import com.nongthinh.agri_catalog_service.application.view.AiModelDeploymentView;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetAiModelDeploymentsUseCaseImpl implements GetAiModelDeploymentsUseCase {

    private final AiModelDeploymentRepository aiModelDeploymentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AiModelDeploymentView> execute() {
        return aiModelDeploymentRepository.findAll().stream()
                .map(AiModelDeploymentView::from)
                .toList();
    }
}
