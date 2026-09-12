package com.nongthinh.agri_catalog_service.infra.persistence.aimodel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaAiModelRepository extends JpaRepository<JpaAiModelEntity, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<JpaAiModelEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<JpaAiModelEntity> findAllByDeletedAtIsNullOrderByCreatedAtDesc();
}
