package com.nongthinh.rice_disease_diagnosis_service.infra.persistence.diagnosishistory;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class DiagnosisHistoryJsonPersistenceTest {

    @Autowired
    private JpaDiagnosisHistoryRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAndReadsResultSnapshotAsJson() {
        UUID historyId = UUID.randomUUID();
        String snapshot = "{\"status\":\"HEALTHY\",\"images\":[]}";
        JpaDiagnosisHistoryEntity entity = JpaDiagnosisHistoryEntity.builder()
                .id(historyId)
                .farmerUserId(UUID.randomUUID())
                .cropTypeId(UUID.randomUUID())
                .modelId(UUID.randomUUID())
                .modelVersionId(UUID.randomUUID())
                .modelVersion("1.0.0")
                .status("HEALTHY")
                .resultSnapshot(snapshot)
                .fileIds(List.of())
                .createdAt(Instant.now())
                .build();

        repository.saveAndFlush(entity);
        entityManager.clear();

        JpaDiagnosisHistoryEntity persisted = repository.findById(historyId).orElseThrow();
        assertThat(persisted.getResultSnapshot()).isEqualTo(snapshot);
    }
}
