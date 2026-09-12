package com.nongthinh.rice_disease_diagnosis_service.application.service;

import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.DiagnosisFilePort;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.repository.DiagnosisImageCleanupTaskRepository;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.DiagnosisImageCleanupTask;
import lombok.RequiredArgsConstructor;

/**
 * Processes the history-retention outbox. A failed file-service call is retried
 * later and never rolls back a completed diagnosis or leaves the image exposed.
 */
@Component
@RequiredArgsConstructor
public class DiagnosisImageCleanupProcessor {

    private static final int BATCH_SIZE = 50;

    private final DiagnosisImageCleanupTaskRepository cleanupTaskRepository;
    private final DiagnosisFilePort diagnosisFilePort;

    @Scheduled(fixedDelayString = "${diagnosis.cleanup.fixed-delay-ms:60000}")
    public void processScheduledBatch() {
        processPending();
    }

    public void processPending() {
        cleanupTaskRepository.findPending(BATCH_SIZE).forEach(this::process);
    }

    private void process(DiagnosisImageCleanupTask task) {
        Instant now = Instant.now();
        try {
            if (!cleanupTaskRepository.isReferencedByExistingHistory(task.getFileId())) {
                diagnosisFilePort.deleteDiagnosisImage(task.getFileId());
            }
            task.complete(now);
        } catch (RuntimeException ignored) {
            // The task remains pending and is retried. Do not log upstream bodies or headers.
            task.fail(now);
        }
        cleanupTaskRepository.save(task);
    }
}
