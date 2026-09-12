package com.nongthinh.agri_catalog_service.infra.persistence.aimodelversion;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaAiModelVersionClassRepository extends JpaRepository<JpaAiModelVersionClassEntity, UUID> {

    List<JpaAiModelVersionClassEntity> findAllByModelVersionIdOrderByClassIndexAsc(UUID modelVersionId);
}
