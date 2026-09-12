package com.nongthinh.rice_disease_diagnosis_service.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.DiagnosisFilePort;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.repository.DiagnosisImageCleanupTaskRepository;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.DiagnosisImageCleanupTask;

class DiagnosisImageCleanupProcessorTest {

    @Test
    void deletesAnUnreferencedImageAndCompletesTheDurableTask() {
        UUID fileId = UUID.randomUUID();
        DiagnosisImageCleanupTask task = task(fileId);
        DiagnosisImageCleanupTaskRepository taskRepository = mock(DiagnosisImageCleanupTaskRepository.class);
        DiagnosisFilePort filePort = mock(DiagnosisFilePort.class);
        when(taskRepository.findPending(50)).thenReturn(List.of(task));
        when(taskRepository.isReferencedByExistingHistory(fileId)).thenReturn(false);

        new DiagnosisImageCleanupProcessor(taskRepository, filePort).processPending();

        verify(filePort).deleteDiagnosisImage(fileId);
        verify(taskRepository).save(task);
        org.assertj.core.api.Assertions.assertThat(task.getCompletedAt()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(task.getAttempts()).isEqualTo(1);
    }

    @Test
    void keepsAnImageWhenAnotherHistoryStillReferencesIt() {
        UUID fileId = UUID.randomUUID();
        DiagnosisImageCleanupTask task = task(fileId);
        DiagnosisImageCleanupTaskRepository taskRepository = mock(DiagnosisImageCleanupTaskRepository.class);
        DiagnosisFilePort filePort = mock(DiagnosisFilePort.class);
        when(taskRepository.findPending(50)).thenReturn(List.of(task));
        when(taskRepository.isReferencedByExistingHistory(fileId)).thenReturn(true);

        new DiagnosisImageCleanupProcessor(taskRepository, filePort).processPending();

        verify(filePort, never()).deleteDiagnosisImage(any());
        verify(taskRepository).save(task);
        org.assertj.core.api.Assertions.assertThat(task.getCompletedAt()).isNotNull();
    }

    @Test
    void retainsATaskForRetryWhenFileServiceIsUnavailable() {
        UUID fileId = UUID.randomUUID();
        DiagnosisImageCleanupTask task = task(fileId);
        DiagnosisImageCleanupTaskRepository taskRepository = mock(DiagnosisImageCleanupTaskRepository.class);
        DiagnosisFilePort filePort = mock(DiagnosisFilePort.class);
        when(taskRepository.findPending(50)).thenReturn(List.of(task));
        when(taskRepository.isReferencedByExistingHistory(fileId)).thenReturn(false);
        org.mockito.Mockito.doThrow(new IllegalStateException()).when(filePort).deleteDiagnosisImage(fileId);

        new DiagnosisImageCleanupProcessor(taskRepository, filePort).processPending();

        verify(taskRepository).save(task);
        org.assertj.core.api.Assertions.assertThat(task.getCompletedAt()).isNull();
        org.assertj.core.api.Assertions.assertThat(task.getAttempts()).isEqualTo(1);
    }

    private DiagnosisImageCleanupTask task(UUID fileId) {
        return DiagnosisImageCleanupTask.create(
                UUID.randomUUID(), UUID.randomUUID(), fileId, Instant.parse("2026-08-11T00:00:00Z"));
    }
}
