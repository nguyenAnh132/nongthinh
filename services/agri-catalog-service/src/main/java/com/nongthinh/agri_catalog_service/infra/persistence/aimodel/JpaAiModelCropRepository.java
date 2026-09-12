package com.nongthinh.agri_catalog_service.infra.persistence.aimodel;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaAiModelCropRepository extends JpaRepository<JpaAiModelCropEntity, JpaAiModelCropId> {

    boolean existsByIdCropTypeId(UUID cropTypeId);

    List<JpaAiModelCropEntity> findAllByIdModelId(UUID modelId);

    void deleteByIdModelId(UUID modelId);
}
