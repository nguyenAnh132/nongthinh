package com.nongthinh.rice_disease_diagnosis_service.application.port.in.diagnosis.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import com.nongthinh.rice_disease_diagnosis_service.application.command.DiagnosisCommand;
import com.nongthinh.rice_disease_diagnosis_service.application.model.ActiveModelDeployment;
import com.nongthinh.rice_disease_diagnosis_service.application.model.BoundingBox;
import com.nongthinh.rice_disease_diagnosis_service.application.model.DiagnosisFileMetadata;
import com.nongthinh.rice_disease_diagnosis_service.application.model.InferenceDetection;
import com.nongthinh.rice_disease_diagnosis_service.application.model.InferenceImageResult;
import com.nongthinh.rice_disease_diagnosis_service.application.model.ModelClassManifest;
import com.nongthinh.rice_disease_diagnosis_service.application.model.ResolvedDiseaseMapping;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.DiagnosisFilePort;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.ModelRegistryPort;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.repository.DiagnosisHistoryRepository;
import com.nongthinh.rice_disease_diagnosis_service.application.service.OnnxInferenceEngine;
import com.nongthinh.rice_disease_diagnosis_service.application.view.DiagnosisResponseView;
import com.nongthinh.rice_disease_diagnosis_service.common.currentuser.CurrentUser;
import com.nongthinh.rice_disease_diagnosis_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.DiagnosisException;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.ErrorCode;
import com.nongthinh.rice_disease_diagnosis_service.configuration.DiagnosisProperties;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.DiagnosisHistory;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.valueobject.DiagnosisStatus;

class CreateDiagnosisUseCaseImplTest {

    private final UUID farmerUserId = UUID.randomUUID();
    private final UUID cropTypeId = UUID.randomUUID();
    private final UUID fileId = UUID.randomUUID();
    private final UUID modelVersionId = UUID.randomUUID();
    private CurrentUserProvider currentUserProvider;
    private ModelRegistryPort modelRegistryPort;
    private DiagnosisFilePort diagnosisFilePort;
    private OnnxInferenceEngine inferenceEngine;
    private DiagnosisHistoryRepository historyRepository;
    private CreateDiagnosisUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        currentUserProvider = mock(CurrentUserProvider.class);
        modelRegistryPort = mock(ModelRegistryPort.class);
        diagnosisFilePort = mock(DiagnosisFilePort.class);
        inferenceEngine = mock(OnnxInferenceEngine.class);
        historyRepository = mock(DiagnosisHistoryRepository.class);
        useCase = new CreateDiagnosisUseCaseImpl(
                currentUserProvider, modelRegistryPort, diagnosisFilePort, inferenceEngine, historyRepository,
                new DiagnosisProperties(5, 5_000_000, 40_000_000, .25f, .45f, 4),
                new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void resolvesDeploymentFromCropAndPersistsImmutableDiagnosisSnapshot() {
        ActiveModelDeployment deployment = deployment();
        when(currentUserProvider.getCurrentUser()).thenReturn(new CurrentUser(farmerUserId));
        when(modelRegistryPort.resolveActiveDeployment(cropTypeId)).thenReturn(deployment);
        when(diagnosisFilePort.getFileMetadata(fileId)).thenReturn(file());
        when(diagnosisFilePort.getFileContent(fileId)).thenReturn(new byte[] {1, 2, 3});
        when(inferenceEngine.infer(eq(deployment), any(byte[].class))).thenReturn(new InferenceImageResult(
                640, 480, List.of(new InferenceDetection(
                        0, "RICE_BLAST", "Rice blast", "DISEASE", .91f, new BoundingBox(1, 2, 3, 4)))));
        UUID diseaseId = UUID.randomUUID();
        when(modelRegistryPort.resolveDiseaseMappings(modelVersionId, cropTypeId, List.of("RICE_BLAST")))
                .thenReturn(List.of(new ResolvedDiseaseMapping("RICE_BLAST", diseaseId, "Rice blast", Instant.now())));
        when(historyRepository.appendAndTrim(any(DiagnosisHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DiagnosisResponseView result = useCase.execute(new DiagnosisCommand(cropTypeId, List.of(fileId)));

        assertThat(result.status()).isEqualTo(DiagnosisStatus.DISEASED);
        assertThat(result.model().version()).isEqualTo("v1");
        assertThat(result.images().getFirst().detections().getFirst().disease().id()).isEqualTo(diseaseId);
        verify(modelRegistryPort).resolveActiveDeployment(cropTypeId);
        ArgumentCaptor<DiagnosisHistory> historyCaptor = ArgumentCaptor.forClass(DiagnosisHistory.class);
        verify(historyRepository).appendAndTrim(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getResultSnapshot())
                .contains("RICE_BLAST")
                .doesNotContain("artifactFileId")
                .doesNotContain("api-key");
    }

    @Test
    void rejectsAnInvalidFileListBeforeResolvingAnyRuntimeOrModel() {
        List<UUID> fileIds = List.of(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        assertThatThrownBy(() -> useCase.execute(new DiagnosisCommand(cropTypeId, fileIds)))
                .isInstanceOf(DiagnosisException.class)
                .extracting(exception -> ((DiagnosisException) exception).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REQUEST);
        verifyNoInteractions(currentUserProvider, modelRegistryPort, diagnosisFilePort, inferenceEngine, historyRepository);
    }

    private ActiveModelDeployment deployment() {
        return new ActiveModelDeployment(
                UUID.randomUUID(), UUID.randomUUID(), "RICE", "Rice detector", modelVersionId, "v1",
                UUID.randomUUID(), "a".repeat(64), 640, 640,
                List.of(new ModelClassManifest(UUID.randomUUID(), 0, "RICE_BLAST", "Rice blast", "DISEASE")));
    }

    private DiagnosisFileMetadata file() {
        return new DiagnosisFileMetadata(
                fileId, farmerUserId, "DIAGNOSIS_IMAGE", "leaf.jpg", "image/jpeg", 3,
                "UPLOADED", Instant.now(), Instant.now());
    }
}
