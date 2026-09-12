package com.nongthinh.agri_catalog_service.infra.persistence.aimodeldiseasemapping;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaAiModelDiseaseMappingRepository
        extends JpaRepository<JpaAiModelDiseaseMappingEntity, UUID> {

    boolean existsByModelVersionClassIdAndCropTypeId(UUID modelVersionClassId, UUID cropTypeId);

    boolean existsByModelVersionClassIdAndCropTypeIdAndIdNot(
            UUID modelVersionClassId, UUID cropTypeId, UUID id);

    @Query("""
            SELECT m
            FROM JpaAiModelDiseaseMappingEntity m
            JOIN FETCH m.modelVersionClass c
            WHERE c.modelVersionId = :modelVersionId
            ORDER BY c.classIndex ASC, m.cropTypeId ASC
            """)
    List<JpaAiModelDiseaseMappingEntity> findAllByModelVersionId(
            @Param("modelVersionId") UUID modelVersionId);

    @Query("""
            SELECT m
            FROM JpaAiModelDiseaseMappingEntity m
            JOIN FETCH m.disease d
            WHERE m.modelVersionClassId IN :classIds
              AND m.cropTypeId IN :cropTypeIds
              AND d.deletedAt IS NULL
              AND d.reviewStatus = 'APPROVED'
              AND d.cropTypeId = m.cropTypeId
            """)
    List<JpaAiModelDiseaseMappingEntity> findAllValidByClassIdsAndCropTypeIds(
            @Param("classIds") Collection<UUID> classIds,
            @Param("cropTypeIds") Collection<UUID> cropTypeIds);

    @Query("""
            SELECT m
            FROM JpaAiModelDiseaseMappingEntity m
            JOIN FETCH m.modelVersionClass c
            JOIN FETCH m.disease d
            WHERE c.modelVersionId = :modelVersionId
              AND m.cropTypeId = :cropTypeId
              AND c.classCode IN :classCodes
              AND c.classKind = 'DISEASE'
              AND d.deletedAt IS NULL
              AND d.reviewStatus = 'APPROVED'
              AND d.cropTypeId = m.cropTypeId
            """)
    List<JpaAiModelDiseaseMappingEntity> resolveValidMappings(
            @Param("modelVersionId") UUID modelVersionId,
            @Param("cropTypeId") UUID cropTypeId,
            @Param("classCodes") Collection<String> classCodes);
}
