package com.nongthinh.rice_disease_diagnosis_service.application.port.in.diagnosis.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.repository.DiagnosisHistoryRepository;
import com.nongthinh.rice_disease_diagnosis_service.application.view.DiagnosisHistoryDetailView;
import com.nongthinh.rice_disease_diagnosis_service.common.currentuser.CurrentUser;
import com.nongthinh.rice_disease_diagnosis_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.DiagnosisHistory;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.valueobject.DiagnosisStatus;

class GetDiagnosisHistoryUseCaseImplTest {

    @Test
    void returnsTheStoredSnapshotWithoutRecomputingOrExposingAnotherFarmersHistory() {
        UUID farmerUserId = UUID.randomUUID();
        UUID diagnosisId = UUID.randomUUID();
        UUID cropTypeId = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();
        UUID modelVersionId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
        DiagnosisHistoryRepository historyRepository = mock(DiagnosisHistoryRepository.class);
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUser(farmerUserId));
        DiagnosisHistory history = DiagnosisHistory.create(
                diagnosisId, farmerUserId, cropTypeId, modelId, modelVersionId, "v3",
                DiagnosisStatus.DISEASED, """
                        {
                          "diagnosisId": "%s",
                          "cropTypeId": "%s",
                          "model": {
                            "id": "%s",
                            "name": "Rice detector",
                            "versionId": "%s",
                            "version": "v3"
                          },
                          "status": "DISEASED",
                          "images": [{
                            "file": {
                              "id": "%s",
                              "originalFileName": "rice.png",
                              "contentType": "image/png",
                              "sizeBytes": 5,
                              "createdAt": "2026-08-19T12:00:00Z",
                              "updatedAt": "2026-08-19T12:01:00Z"
                            },
                            "width": 1200,
                            "height": 800,
                            "status": "DISEASED",
                            "detections": []
                          }],
                          "groups": [],
                          "processingTimeMs": 125
                        }
                        """.formatted(diagnosisId, cropTypeId, modelId, modelVersionId, fileId), Instant.now());
        when(historyRepository.findByIdAndFarmerUserId(diagnosisId, farmerUserId)).thenReturn(Optional.of(history));
        GetDiagnosisHistoryUseCaseImpl useCase = new GetDiagnosisHistoryUseCaseImpl(
                currentUserProvider, historyRepository, objectMapper);

        DiagnosisHistoryDetailView result = useCase.execute(diagnosisId);

        assertThat(result.snapshot().model().version()).isEqualTo("v3");
        assertThat(result.snapshot().images()).hasSize(1);
        assertThat(result.snapshot().images().getFirst().file().originalFileName()).isEqualTo("rice.png");
        assertThat(result.snapshot().images().getFirst().file().createdAt())
                .isEqualTo(Instant.parse("2026-08-19T12:00:00Z"));

        JsonNode httpResult = objectMapper.valueToTree(result);
        assertThat(httpResult.at("/snapshot/model/version").asText()).isEqualTo("v3");
        assertThat(httpResult.at("/snapshot/images/0/file/id").asText()).isEqualTo(fileId.toString());
        assertThat(httpResult.at("/snapshot/groups").isArray()).isTrue();
        verify(historyRepository).findByIdAndFarmerUserId(diagnosisId, farmerUserId);
    }
}
