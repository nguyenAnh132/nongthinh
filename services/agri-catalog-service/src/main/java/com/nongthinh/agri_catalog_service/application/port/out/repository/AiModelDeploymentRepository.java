package com.nongthinh.agri_catalog_service.application.port.out.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.AiModelDeployment;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.valueobject.AiModelDeploymentScope;

public interface AiModelDeploymentRepository {

    Optional<AiModelDeployment> findById(UUID id);

    List<AiModelDeployment> findAll();

    List<AiModelDeployment> findActiveForCrop(UUID cropTypeId);

    List<AiModelDeployment> findActiveForAllCrops();

    boolean existsActiveByModelVersionId(UUID modelVersionId);

    Optional<AiModelDeployment> findActiveWithScopeCropAndPriority(
            AiModelDeploymentScope deploymentScope,
            UUID cropTypeId,
            int priority,
            UUID excludedDeploymentId);

    AiModelDeployment save(AiModelDeployment deployment);

    AiModelDeployment saveAndFlush(AiModelDeployment deployment);
}
