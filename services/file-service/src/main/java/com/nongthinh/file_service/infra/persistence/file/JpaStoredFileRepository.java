package com.nongthinh.file_service.infra.persistence.file;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaStoredFileRepository extends JpaRepository<JpaStoredFileEntity, UUID> {

    List<JpaStoredFileEntity> findByOwnerUserIdAndPurposeOrderByCreatedAtDesc(UUID ownerUserId, String purpose);
}
