package com.nongthinh.rice_disease_diagnosis_service.infra.persistence.diagnosishistory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.repository.DiagnosisImageCleanupTaskRepository;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.DiagnosisImageCleanupTask;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DiagnosisImageCleanupTaskRepositoryImpl implements DiagnosisImageCleanupTaskRepository {

    private final JpaDiagnosisImageCleanupTaskRepository jpaRepository;
    private final DiagnosisImageCleanupTaskPersistenceMapper mapper;

    @Override
    public void enqueue(UUID diagnosisHistoryId, UUID fileId, Instant createdAt) {
        jpaRepository.save(JpaDiagnosisImageCleanupTaskEntity.builder()
                .id(UUID.randomUUID())
                .diagnosisHistoryId(diagnosisHistoryId)
                .fileId(fileId)
                .createdAt(createdAt)
                .attempts(0)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiagnosisImageCleanupTask> findPending(int limit) {
        if (limit <= 0) return List.of();
        return jpaRepository.findTop50ByCompletedAtIsNullOrderByCreatedAtAsc().stream()
                .limit(limit)
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isReferencedByExistingHistory(UUID fileId) {
        return jpaRepository.existsHistoryReference(fileId);
    }

    @Override
    public DiagnosisImageCleanupTask save(DiagnosisImageCleanupTask task) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(task)));
    }
}
