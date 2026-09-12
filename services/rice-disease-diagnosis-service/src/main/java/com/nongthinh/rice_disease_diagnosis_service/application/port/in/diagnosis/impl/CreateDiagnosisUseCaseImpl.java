package com.nongthinh.rice_disease_diagnosis_service.application.port.in.diagnosis.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.rice_disease_diagnosis_service.application.command.DiagnosisCommand;
import com.nongthinh.rice_disease_diagnosis_service.application.model.ActiveModelDeployment;
import com.nongthinh.rice_disease_diagnosis_service.application.model.DiagnosisFileMetadata;
import com.nongthinh.rice_disease_diagnosis_service.application.model.InferenceDetection;
import com.nongthinh.rice_disease_diagnosis_service.application.model.InferenceImageResult;
import com.nongthinh.rice_disease_diagnosis_service.application.model.ResolvedDiseaseMapping;
import com.nongthinh.rice_disease_diagnosis_service.application.port.in.diagnosis.CreateDiagnosisUseCase;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.DiagnosisFilePort;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.ModelRegistryPort;
import com.nongthinh.rice_disease_diagnosis_service.application.port.out.repository.DiagnosisHistoryRepository;
import com.nongthinh.rice_disease_diagnosis_service.application.service.OnnxInferenceEngine;
import com.nongthinh.rice_disease_diagnosis_service.application.view.DiagnosisBoundingBoxView;
import com.nongthinh.rice_disease_diagnosis_service.application.view.DiagnosisDetectionView;
import com.nongthinh.rice_disease_diagnosis_service.application.view.DiagnosisDiseaseReferenceView;
import com.nongthinh.rice_disease_diagnosis_service.application.view.DiagnosisFileView;
import com.nongthinh.rice_disease_diagnosis_service.application.view.DiagnosisGroupView;
import com.nongthinh.rice_disease_diagnosis_service.application.view.DiagnosisImageView;
import com.nongthinh.rice_disease_diagnosis_service.application.view.DiagnosisModelView;
import com.nongthinh.rice_disease_diagnosis_service.application.view.DiagnosisResponseView;
import com.nongthinh.rice_disease_diagnosis_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.DiagnosisException;
import com.nongthinh.rice_disease_diagnosis_service.common.exception.ErrorCode;
import com.nongthinh.rice_disease_diagnosis_service.configuration.DiagnosisProperties;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.DiagnosisHistory;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.valueobject.DiagnosisStatus;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateDiagnosisUseCaseImpl implements CreateDiagnosisUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final ModelRegistryPort modelRegistryPort;
    private final DiagnosisFilePort diagnosisFilePort;
    private final OnnxInferenceEngine onnxInferenceEngine;
    private final DiagnosisHistoryRepository diagnosisHistoryRepository;
    private final DiagnosisProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public DiagnosisResponseView execute(DiagnosisCommand command) {
        validate(command);
        long startedAt = System.nanoTime();
        UUID farmerUserId = currentUserProvider.getCurrentUser().userId();
        ActiveModelDeployment deployment = modelRegistryPort.resolveActiveDeployment(command.cropTypeId());
        List<ImageInference> inferences = command.fileIds().stream()
                .map(fileId -> inferFile(farmerUserId, deployment, fileId))
                .toList();

        Map<String, ResolvedDiseaseMapping> mappings = resolveMappings(command.cropTypeId(), deployment, inferences);
        List<DiagnosisImageView> images = inferences.stream()
                .map(item -> toImageView(item, mappings))
                .toList();
        DiagnosisStatus status = aggregateStatus(images);
        List<DiagnosisGroupView> groups = groups(images);
        UUID diagnosisId = UUID.randomUUID();
        long processingTimeMs = (System.nanoTime() - startedAt) / 1_000_000;
        DiagnosisResponseView response = new DiagnosisResponseView(
                diagnosisId,
                command.cropTypeId(),
                new DiagnosisModelView(
                        deployment.modelId(), deployment.modelName(), deployment.modelVersionId(), deployment.modelVersion()),
                status,
                images,
                groups,
                processingTimeMs);
        diagnosisHistoryRepository.appendAndTrim(DiagnosisHistory.create(
                diagnosisId,
                farmerUserId,
                command.cropTypeId(),
                deployment.modelId(),
                deployment.modelVersionId(),
                deployment.modelVersion(),
                status,
                snapshot(response),
                command.fileIds(),
                Instant.now()));
        return response;
    }

    private ImageInference inferFile(UUID farmerUserId, ActiveModelDeployment deployment, UUID fileId) {
        DiagnosisFileMetadata file = metadata(fileId);
        if (!file.isActiveDiagnosisImageOwnedBy(farmerUserId)
                || file.sizeBytes() <= 0
                || file.sizeBytes() > properties.maxImageBytes()) {
            throw new DiagnosisException(ErrorCode.INVALID_REQUEST);
        }
        byte[] content = content(fileId);
        if (content.length == 0 || content.length > properties.maxImageBytes()) {
            throw new DiagnosisException(ErrorCode.INVALID_REQUEST);
        }
        return new ImageInference(file, onnxInferenceEngine.infer(deployment, content));
    }

    private DiagnosisFileMetadata metadata(UUID fileId) {
        try {
            return diagnosisFilePort.getFileMetadata(fileId);
        } catch (DiagnosisException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new DiagnosisException(ErrorCode.FILE_SERVICE_UNAVAILABLE, ex);
        }
    }

    private byte[] content(UUID fileId) {
        try {
            return diagnosisFilePort.getFileContent(fileId);
        } catch (DiagnosisException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new DiagnosisException(ErrorCode.FILE_SERVICE_UNAVAILABLE, ex);
        }
    }

    private Map<String, ResolvedDiseaseMapping> resolveMappings(
            UUID cropTypeId, ActiveModelDeployment deployment, List<ImageInference> inferences) {
        List<String> classCodes = inferences.stream()
                .flatMap(item -> item.result.detections().stream())
                .filter(InferenceDetection::isDisease)
                .map(InferenceDetection::classCode)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new), ArrayList::new));
        if (classCodes.isEmpty()) return Map.of();
        List<ResolvedDiseaseMapping> resolved = modelRegistryPort.resolveDiseaseMappings(
                deployment.modelVersionId(), cropTypeId, classCodes);
        Map<String, ResolvedDiseaseMapping> mappings = resolved.stream()
                .collect(java.util.stream.Collectors.toMap(ResolvedDiseaseMapping::classCode, item -> item));
        if (mappings.size() != classCodes.size() || !mappings.keySet().containsAll(classCodes)) {
            throw new DiagnosisException(ErrorCode.CATALOG_MAPPING_NOT_READY);
        }
        return mappings;
    }

    private DiagnosisImageView toImageView(ImageInference inference, Map<String, ResolvedDiseaseMapping> mappings) {
        List<DiagnosisDetectionView> detections = inference.result.detections().stream()
                .map(detection -> toDetectionView(detection, mappings.get(detection.classCode())))
                .toList();
        return new DiagnosisImageView(
                DiagnosisFileView.from(inference.file),
                inference.result.width(),
                inference.result.height(),
                imageStatus(detections),
                detections);
    }

    private DiagnosisDetectionView toDetectionView(
            InferenceDetection detection, ResolvedDiseaseMapping mapping) {
        DiagnosisDiseaseReferenceView disease = detection.isDisease()
                ? new DiagnosisDiseaseReferenceView(mapping.diseaseId(), mapping.displayName())
                : null;
        return new DiagnosisDetectionView(
                detection.classCode(), detection.displayName(), detection.classKind(), detection.confidence(),
                DiagnosisBoundingBoxView.from(detection.boundingBox()), disease);
    }

    private DiagnosisStatus aggregateStatus(List<DiagnosisImageView> images) {
        if (images.stream().anyMatch(item -> item.status() == DiagnosisStatus.DISEASED)) return DiagnosisStatus.DISEASED;
        if (images.stream().anyMatch(item -> item.status() == DiagnosisStatus.HEALTHY)) return DiagnosisStatus.HEALTHY;
        return DiagnosisStatus.UNDETERMINED;
    }

    private DiagnosisStatus imageStatus(List<DiagnosisDetectionView> detections) {
        if (detections.stream().anyMatch(item -> "DISEASE".equals(item.classKind()))) return DiagnosisStatus.DISEASED;
        if (detections.stream().anyMatch(item -> "HEALTHY".equals(item.classKind()))) return DiagnosisStatus.HEALTHY;
        return DiagnosisStatus.UNDETERMINED;
    }

    private List<DiagnosisGroupView> groups(List<DiagnosisImageView> images) {
        Map<GroupKey, GroupAccumulator> grouped = new LinkedHashMap<>();
        for (DiagnosisImageView image : images) {
            for (DiagnosisDetectionView detection : image.detections()) {
                DiagnosisStatus status = "DISEASE".equals(detection.classKind())
                        ? DiagnosisStatus.DISEASED
                        : DiagnosisStatus.HEALTHY;
                GroupKey key = new GroupKey(status, detection.classCode(), detection.disease() == null ? null : detection.disease().id());
                grouped.computeIfAbsent(key, ignored -> new GroupAccumulator(status, detection))
                        .add(image.file().id());
            }
        }
        return grouped.values().stream().map(GroupAccumulator::toView).toList();
    }

    private String snapshot(DiagnosisResponseView response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException ex) {
            throw new DiagnosisException(ErrorCode.INTERNAL_ERROR, ex);
        }
    }

    private void validate(DiagnosisCommand command) {
        if (command == null || command.cropTypeId() == null || command.fileIds() == null
                || command.fileIds().isEmpty() || command.fileIds().size() > properties.maxFilesPerRequest()
                || command.fileIds().stream().anyMatch(java.util.Objects::isNull)
                || new LinkedHashSet<>(command.fileIds()).size() != command.fileIds().size()) {
            throw new DiagnosisException(ErrorCode.INVALID_REQUEST);
        }
    }

    private record ImageInference(DiagnosisFileMetadata file, InferenceImageResult result) {
    }

    private record GroupKey(DiagnosisStatus status, String classCode, UUID diseaseId) {
    }

    private static final class GroupAccumulator {
        private final DiagnosisStatus status;
        private final DiagnosisDetectionView detection;
        private final LinkedHashSet<UUID> fileIds = new LinkedHashSet<>();
        private int detectionCount;

        private GroupAccumulator(DiagnosisStatus status, DiagnosisDetectionView detection) {
            this.status = status;
            this.detection = detection;
        }

        private void add(UUID fileId) {
            fileIds.add(fileId);
            detectionCount++;
        }

        private DiagnosisGroupView toView() {
            return new DiagnosisGroupView(
                    status, detection.classCode(), detection.displayName(), detection.disease(),
                    List.copyOf(fileIds), detectionCount);
        }
    }
}
