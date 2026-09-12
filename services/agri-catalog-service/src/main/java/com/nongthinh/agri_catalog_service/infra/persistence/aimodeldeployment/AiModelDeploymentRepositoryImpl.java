package com.nongthinh.agri_catalog_service.infra.persistence.aimodeldeployment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelDeploymentRepository;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.AiModelDeployment;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.valueobject.AiModelDeploymentScope;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.valueobject.AiModelDeploymentStatus;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AiModelDeploymentRepositoryImpl implements AiModelDeploymentRepository {

    private final JpaAiModelDeploymentRepository jpaAiModelDeploymentRepository;
    private final AiModelDeploymentPersistenceMapper mapper;

    @Override
    public Optional<AiModelDeployment> findById(UUID id) {
        return jpaAiModelDeploymentRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<AiModelDeployment> findAll() {
        return jpaAiModelDeploymentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AiModelDeployment> findActiveForCrop(UUID cropTypeId) {
        return jpaAiModelDeploymentRepository
                .findAllByStatusAndDeploymentScopeAndCropTypeIdOrderByPriorityAsc(
                        AiModelDeploymentStatus.ACTIVE.name(),
                        AiModelDeploymentScope.CROP.name(),
                        cropTypeId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<AiModelDeployment> findActiveForAllCrops() {
        return jpaAiModelDeploymentRepository
                .findAllByStatusAndDeploymentScopeAndCropTypeIdIsNullOrderByPriorityAsc(
                        AiModelDeploymentStatus.ACTIVE.name(),
                        AiModelDeploymentScope.ALL_CROPS.name())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsActiveByModelVersionId(UUID modelVersionId) {
        return jpaAiModelDeploymentRepository.existsByModelVersionIdAndStatus(
                modelVersionId, AiModelDeploymentStatus.ACTIVE.name());
    }

    @Override
    public Optional<AiModelDeployment> findActiveWithScopeCropAndPriority(
            AiModelDeploymentScope deploymentScope,
            UUID cropTypeId,
            int priority,
            UUID excludedDeploymentId) {
        Optional<JpaAiModelDeploymentEntity> active = deploymentScope == AiModelDeploymentScope.ALL_CROPS
                ? jpaAiModelDeploymentRepository
                        .findFirstByStatusAndDeploymentScopeAndCropTypeIdIsNullAndPriorityAndIdNot(
                                AiModelDeploymentStatus.ACTIVE.name(),
                                deploymentScope.name(),
                                priority,
                                excludedDeploymentId)
                : jpaAiModelDeploymentRepository
                        .findFirstByStatusAndDeploymentScopeAndCropTypeIdAndPriorityAndIdNot(
                                AiModelDeploymentStatus.ACTIVE.name(),
                                deploymentScope.name(),
                                cropTypeId,
                                priority,
                                excludedDeploymentId);
        return active.map(mapper::toDomain);
    }

    @Override
    public AiModelDeployment save(AiModelDeployment deployment) {
        return mapper.toDomain(jpaAiModelDeploymentRepository.save(mapper.toEntity(deployment)));
    }

    @Override
    public AiModelDeployment saveAndFlush(AiModelDeployment deployment) {
        return mapper.toDomain(jpaAiModelDeploymentRepository.saveAndFlush(mapper.toEntity(deployment)));
    }
}
