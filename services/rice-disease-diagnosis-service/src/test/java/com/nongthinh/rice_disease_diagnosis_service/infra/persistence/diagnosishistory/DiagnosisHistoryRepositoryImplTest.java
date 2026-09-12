package com.nongthinh.rice_disease_diagnosis_service.infra.persistence.diagnosishistory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.DiagnosisHistory;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.repository.DiagnosisImageCleanupTaskRepository;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.valueobject.DiagnosisStatus;

class DiagnosisHistoryRepositoryImplTest {

    @Test
    void keepsOnlyTheNewestTenRecordsAcrossElevenSequentialScansForOneFarmer() {
        UUID farmerUserId = UUID.randomUUID();
        JpaDiagnosisHistoryRepository historyJpaRepository = mock(JpaDiagnosisHistoryRepository.class);
        JpaFarmerDiagnosisHistoryLockRepository lockJpaRepository = mock(JpaFarmerDiagnosisHistoryLockRepository.class);
        DiagnosisImageCleanupTaskRepository cleanupTaskRepository = mock(DiagnosisImageCleanupTaskRepository.class);
        DiagnosisHistoryRepositoryImpl repository = new DiagnosisHistoryRepositoryImpl(
                historyJpaRepository, lockJpaRepository, new DiagnosisHistoryPersistenceMapper(), cleanupTaskRepository);
        List<JpaDiagnosisHistoryEntity> persisted = new ArrayList<>();
        when(lockJpaRepository.findByFarmerUserId(farmerUserId))
                .thenReturn(Optional.of(new JpaFarmerDiagnosisHistoryLockEntity(farmerUserId)));
        when(historyJpaRepository.findAllByFarmerUserIdOrderByCreatedAtDescIdDesc(farmerUserId))
                .thenAnswer(ignored -> persisted.stream()
                        .sorted(Comparator.comparing(JpaDiagnosisHistoryEntity::getCreatedAt).reversed()
                                .thenComparing(JpaDiagnosisHistoryEntity::getId, Comparator.reverseOrder()))
                        .toList());
        doAnswer(invocation -> {
            Iterable<JpaDiagnosisHistoryEntity> records = invocation.getArgument(0);
            records.forEach(persisted::remove);
            return null;
        }).when(historyJpaRepository).deleteAll(any());
        when(historyJpaRepository.save(any(JpaDiagnosisHistoryEntity.class))).thenAnswer(invocation -> {
            JpaDiagnosisHistoryEntity entity = invocation.getArgument(0);
            persisted.add(entity);
            return entity;
        });

        List<UUID> historyIds = new ArrayList<>();
        Instant startedAt = Instant.parse("2026-01-01T00:00:00Z");
        for (int index = 0; index < 11; index++) {
            UUID historyId = UUID.randomUUID();
            historyIds.add(historyId);
            repository.appendAndTrim(DiagnosisHistory.create(
                    historyId, farmerUserId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "v1",
                    DiagnosisStatus.HEALTHY, "{\"images\":[]}",
                    List.of(UUID.nameUUIDFromBytes(("file-" + index).getBytes())), startedAt.plusSeconds(index)));
        }

        assertThat(persisted).hasSize(10);
        assertThat(persisted).extracting(JpaDiagnosisHistoryEntity::getId)
                .doesNotContain(historyIds.getFirst())
                .containsExactlyInAnyOrderElementsOf(historyIds.subList(1, 11));
        verify(lockJpaRepository, times(11)).insertIfAbsent(farmerUserId);
        verify(lockJpaRepository, times(11)).findByFarmerUserId(farmerUserId);
        verify(cleanupTaskRepository, times(1)).enqueue(
                org.mockito.ArgumentMatchers.eq(historyIds.getFirst()),
                org.mockito.ArgumentMatchers.any(UUID.class),
                org.mockito.ArgumentMatchers.any(Instant.class));
    }
}
