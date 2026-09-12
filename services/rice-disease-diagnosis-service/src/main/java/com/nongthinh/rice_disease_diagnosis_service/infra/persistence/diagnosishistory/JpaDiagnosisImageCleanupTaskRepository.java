package com.nongthinh.rice_disease_diagnosis_service.infra.persistence.diagnosishistory;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaDiagnosisImageCleanupTaskRepository
        extends JpaRepository<JpaDiagnosisImageCleanupTaskEntity, UUID> {

    List<JpaDiagnosisImageCleanupTaskEntity> findTop50ByCompletedAtIsNullOrderByCreatedAtAsc();

    @Query("""
            select case when count(history) > 0 then true else false end
            from JpaDiagnosisHistoryEntity history join history.fileIds fileId
            where fileId = :fileId
            """)
    boolean existsHistoryReference(@Param("fileId") UUID fileId);
}
