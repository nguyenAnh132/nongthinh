package com.nongthinh.agri_catalog_service.infra.persistence.aimodeldiseasemapping;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import com.nongthinh.agri_catalog_service.application.model.ResolvedAiModelDiseaseMapping;
import com.nongthinh.agri_catalog_service.application.port.out.repository.AiModelDiseaseMappingRepository;
import com.nongthinh.agri_catalog_service.domain.aimodeldiseasemapping.AiModelDiseaseMapping;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AiModelDiseaseMappingRepositoryImpl implements AiModelDiseaseMappingRepository {

    private final JpaAiModelDiseaseMappingRepository jpaRepository;
    private final AiModelDiseaseMappingPersistenceMapper mapper;

    @Override
    public boolean existsByModelVersionClassIdAndCropTypeId(UUID modelVersionClassId, UUID cropTypeId) {
        return jpaRepository.existsByModelVersionClassIdAndCropTypeId(modelVersionClassId, cropTypeId);
    }

    @Override
    public boolean existsByModelVersionClassIdAndCropTypeIdAndIdNot(
            UUID modelVersionClassId, UUID cropTypeId, UUID id) {
        return jpaRepository.existsByModelVersionClassIdAndCropTypeIdAndIdNot(modelVersionClassId, cropTypeId, id);
    }

    @Override
    public Optional<AiModelDiseaseMapping> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<AiModelDiseaseMapping> findAllByModelVersionId(UUID modelVersionId) {
        return jpaRepository.findAllByModelVersionId(modelVersionId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<AiModelDiseaseMapping> findAllValidByClassIdsAndCropTypeIds(
            Collection<UUID> modelVersionClassIds, Collection<UUID> cropTypeIds) {
        if (modelVersionClassIds.isEmpty() || cropTypeIds.isEmpty()) {
            return List.of();
        }
        return jpaRepository.findAllValidByClassIdsAndCropTypeIds(modelVersionClassIds, cropTypeIds).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<ResolvedAiModelDiseaseMapping> resolveValidMappings(
            UUID modelVersionId, UUID cropTypeId, Collection<String> classCodes) {
        if (classCodes.isEmpty()) {
            return List.of();
        }
        return jpaRepository.resolveValidMappings(modelVersionId, cropTypeId, classCodes).stream()
                .map(entity -> new ResolvedAiModelDiseaseMapping(
                        entity.getModelVersionClass().getClassCode(),
                        entity.getDiseaseId(),
                        entity.getDisease().getName(),
                        catalogUpdatedAt(entity)))
                .toList();
    }

    @Override
    public AiModelDiseaseMapping save(AiModelDiseaseMapping mapping) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(mapping)));
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    private static Instant catalogUpdatedAt(JpaAiModelDiseaseMappingEntity mapping) {
        Instant latest = mapping.getCreatedAt();
        latest = later(latest, mapping.getUpdatedAt());
        latest = later(latest, mapping.getDisease().getCreatedAt());
        return later(latest, mapping.getDisease().getUpdatedAt());
    }

    private static Instant later(Instant first, Instant second) {
        return second == null || (first != null && !second.isAfter(first)) ? first : second;
    }
}
