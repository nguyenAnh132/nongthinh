package com.nongthinh.agri_catalog_service.infra.persistence.aimodelversion;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaAiModelVersionRepository extends JpaRepository<JpaAiModelVersionEntity, UUID> {

    boolean existsByModelIdAndVersion(UUID modelId, String version);

    boolean existsByModelId(UUID modelId);

    List<JpaAiModelVersionEntity> findAllByModelIdOrderByCreatedAtDesc(UUID modelId);
}
