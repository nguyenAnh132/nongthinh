package com.nongthinh.agri_catalog_service.application.port.out.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.application.model.ResolvedAiModelDiseaseMapping;
import com.nongthinh.agri_catalog_service.domain.aimodeldiseasemapping.AiModelDiseaseMapping;

public interface AiModelDiseaseMappingRepository {

    boolean existsByModelVersionClassIdAndCropTypeId(UUID modelVersionClassId, UUID cropTypeId);

    boolean existsByModelVersionClassIdAndCropTypeIdAndIdNot(
            UUID modelVersionClassId, UUID cropTypeId, UUID id);

    Optional<AiModelDiseaseMapping> findById(UUID id);

    List<AiModelDiseaseMapping> findAllByModelVersionId(UUID modelVersionId);

    List<AiModelDiseaseMapping> findAllValidByClassIdsAndCropTypeIds(
            Collection<UUID> modelVersionClassIds,
            Collection<UUID> cropTypeIds);

    List<ResolvedAiModelDiseaseMapping> resolveValidMappings(
            UUID modelVersionId,
            UUID cropTypeId,
            Collection<String> classCodes);

    AiModelDiseaseMapping save(AiModelDiseaseMapping mapping);

    void deleteById(UUID id);
}
