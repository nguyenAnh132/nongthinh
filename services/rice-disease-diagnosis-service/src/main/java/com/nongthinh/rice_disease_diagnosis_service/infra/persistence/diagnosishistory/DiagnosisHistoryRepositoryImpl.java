package com.nongthinh.rice_disease_diagnosis_service.infra.persistence.diagnosishistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.repository.DiagnosisHistoryRepository;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.repository.DiagnosisImageCleanupTaskRepository;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.DiagnosisHistory;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DiagnosisHistoryRepositoryImpl implements DiagnosisHistoryRepository {

    private static final int HISTORY_LIMIT = 10;

    private final JpaDiagnosisHistoryRepository historyRepository;
    private final JpaFarmerDiagnosisHistoryLockRepository lockRepository;
    private final DiagnosisHistoryPersistenceMapper mapper;
    private final DiagnosisImageCleanupTaskRepository cleanupTaskRepository;

    @Override
    @Transactional
    public DiagnosisHistory appendAndTrim(DiagnosisHistory history) {
        UUID farmerUserId = history.getFarmerUserId();
        lockRepository.insertIfAbsent(farmerUserId);
        lockRepository.findByFarmerUserId(farmerUserId).orElseThrow();

        List<JpaDiagnosisHistoryEntity> existing =
                historyRepository.findAllByFarmerUserIdOrderByCreatedAtDescIdDesc(farmerUserId);
        if (existing.size() >= HISTORY_LIMIT) {
            List<JpaDiagnosisHistoryEntity> expired = existing.subList(HISTORY_LIMIT - 1, existing.size());
            expired.forEach(item -> item.getFileIds().forEach(fileId ->
                    cleanupTaskRepository.enqueue(item.getId(), fileId, history.getCreatedAt())));
            historyRepository.deleteAll(expired);
        }
        return mapper.toDomain(historyRepository.save(mapper.toEntity(history)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiagnosisHistory> findLatestByFarmerUserId(UUID farmerUserId) {
        return historyRepository.findAllByFarmerUserIdOrderByCreatedAtDescIdDesc(farmerUserId).stream()
                .limit(HISTORY_LIMIT)
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DiagnosisHistory> findByIdAndFarmerUserId(UUID id, UUID farmerUserId) {
        return historyRepository.findByIdAndFarmerUserId(id, farmerUserId).map(mapper::toDomain);
    }
}
