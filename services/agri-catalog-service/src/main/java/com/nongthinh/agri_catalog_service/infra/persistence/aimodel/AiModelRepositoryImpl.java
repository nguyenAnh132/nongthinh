package com.nongthinh.agri_catalog_service.infra.persistence.aimodel;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelRepository;
import com.nongthinh.agri_catalog_service.domain.aimodel.AiModel;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AiModelRepositoryImpl implements AiModelRepository {

    private final JpaAiModelRepository jpaAiModelRepository;
    private final JpaAiModelCropRepository jpaAiModelCropRepository;
    private final AiModelPersistenceMapper aiModelPersistenceMapper;

    @Override
    public boolean existsByCode(String code) {
        String normalizedCode = code == null ? null : code.trim().toUpperCase(Locale.ROOT);
        return jpaAiModelRepository.existsByCodeIgnoreCase(normalizedCode);
    }

    @Override
    public boolean existsByCropTypeId(UUID cropTypeId) {
        return jpaAiModelCropRepository.existsByIdCropTypeId(cropTypeId);
    }

    @Override
    public Optional<AiModel> findById(UUID id) {
        return jpaAiModelRepository.findByIdAndDeletedAtIsNull(id)
                .map(this::toDomain);
    }

    @Override
    public List<AiModel> findAll() {
        return jpaAiModelRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public AiModel save(AiModel aiModel) {
        JpaAiModelEntity saved = jpaAiModelRepository.save(aiModelPersistenceMapper.toEntity(aiModel));
        jpaAiModelCropRepository.deleteByIdModelId(aiModel.getId());
        jpaAiModelCropRepository.saveAll(aiModel.getCropTypeIds().stream()
                .map(cropTypeId -> new JpaAiModelCropEntity(
                        new JpaAiModelCropId(aiModel.getId(), cropTypeId)
                ))
                .toList());
        return toDomain(saved);
    }

    private AiModel toDomain(JpaAiModelEntity entity) {
        Set<UUID> cropTypeIds = jpaAiModelCropRepository.findAllByIdModelId(entity.getId()).stream()
                .map(crop -> crop.getId().getCropTypeId())
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        return aiModelPersistenceMapper.toDomain(entity, cropTypeIds);
    }
}
