package com.nongthinh.agri_catalog_service.infra.persistence.aimodeldeployment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaAiModelDeploymentRepository extends JpaRepository<JpaAiModelDeploymentEntity, UUID> {

    List<JpaAiModelDeploymentEntity> findAllByOrderByCreatedAtDesc();

    List<JpaAiModelDeploymentEntity> findAllByStatusAndDeploymentScopeAndCropTypeIdOrderByPriorityAsc(
            String status,
            String deploymentScope,
            UUID cropTypeId);

    List<JpaAiModelDeploymentEntity> findAllByStatusAndDeploymentScopeAndCropTypeIdIsNullOrderByPriorityAsc(
            String status,
            String deploymentScope);

    boolean existsByModelVersionIdAndStatus(UUID modelVersionId, String status);

    Optional<JpaAiModelDeploymentEntity> findFirstByStatusAndDeploymentScopeAndCropTypeIdAndPriorityAndIdNot(
            String status,
            String deploymentScope,
            UUID cropTypeId,
            int priority,
            UUID id);

    Optional<JpaAiModelDeploymentEntity> findFirstByStatusAndDeploymentScopeAndCropTypeIdIsNullAndPriorityAndIdNot(
            String status,
            String deploymentScope,
            int priority,
            UUID id);
}
