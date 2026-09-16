package com.nongthinh.bo_portal_service.infra.persistence.fileconfig;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaFilePurposeTypeRepository extends JpaRepository<JpaFilePurposeTypeEntity, UUID> {
    @EntityGraph(attributePaths = "fileType")
    List<JpaFilePurposeTypeEntity> findByPurposeOrderByFileTypeCodeAsc(String purpose);

    @EntityGraph(attributePaths = "fileType")
    Optional<JpaFilePurposeTypeEntity> findByPurposeAndFileTypeCode(String purpose, String code);
}
