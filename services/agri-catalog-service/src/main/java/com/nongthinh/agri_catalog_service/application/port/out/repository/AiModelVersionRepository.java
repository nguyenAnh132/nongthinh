package com.nongthinh.agri_catalog_service.application.port.out.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersion;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.AiModelVersionClass;

public interface AiModelVersionRepository {

    boolean existsByModelIdAndVersion(UUID modelId, String version);

    boolean existsByModelId(UUID modelId);

    Optional<AiModelVersion> findById(UUID id);

    Optional<AiModelVersionClass> findClassById(UUID id);

    List<AiModelVersion> findAllByModelId(UUID modelId);

    AiModelVersion save(AiModelVersion version);
}
