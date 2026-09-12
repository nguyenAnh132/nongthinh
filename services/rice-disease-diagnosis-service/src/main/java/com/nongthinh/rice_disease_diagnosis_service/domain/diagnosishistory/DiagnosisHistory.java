package com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory.valueobject.DiagnosisStatus;

public final class DiagnosisHistory {
    private final UUID id;
    private final UUID farmerUserId;
    private final UUID cropTypeId;
    private final UUID modelId;
    private final UUID modelVersionId;
    private final String modelVersion;
    private final DiagnosisStatus status;
    private final String resultSnapshot;
    private final List<UUID> fileIds;
    private final Instant createdAt;

    private DiagnosisHistory(
            UUID id, UUID farmerUserId, UUID cropTypeId, UUID modelId, UUID modelVersionId,
            String modelVersion, DiagnosisStatus status, String resultSnapshot, List<UUID> fileIds, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.farmerUserId = Objects.requireNonNull(farmerUserId, "farmerUserId is required");
        this.cropTypeId = Objects.requireNonNull(cropTypeId, "cropTypeId is required");
        this.modelId = Objects.requireNonNull(modelId, "modelId is required");
        this.modelVersionId = Objects.requireNonNull(modelVersionId, "modelVersionId is required");
        this.modelVersion = requireText(modelVersion, "modelVersion");
        this.status = Objects.requireNonNull(status, "status is required");
        this.resultSnapshot = requireText(resultSnapshot, "resultSnapshot");
        this.fileIds = normalizeFileIds(fileIds);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
    }

    public static DiagnosisHistory create(
            UUID id, UUID farmerUserId, UUID cropTypeId, UUID modelId, UUID modelVersionId,
            String modelVersion, DiagnosisStatus status, String resultSnapshot, Instant createdAt) {
        return new DiagnosisHistory(
                id, farmerUserId, cropTypeId, modelId, modelVersionId,
                modelVersion, status, resultSnapshot, List.of(), createdAt);
    }

    public static DiagnosisHistory create(
            UUID id, UUID farmerUserId, UUID cropTypeId, UUID modelId, UUID modelVersionId,
            String modelVersion, DiagnosisStatus status, String resultSnapshot, List<UUID> fileIds, Instant createdAt) {
        return new DiagnosisHistory(
                id, farmerUserId, cropTypeId, modelId, modelVersionId,
                modelVersion, status, resultSnapshot, fileIds, createdAt);
    }

    public static DiagnosisHistory reconstruct(
            UUID id, UUID farmerUserId, UUID cropTypeId, UUID modelId, UUID modelVersionId,
            String modelVersion, DiagnosisStatus status, String resultSnapshot, Instant createdAt) {
        return new DiagnosisHistory(
                id, farmerUserId, cropTypeId, modelId, modelVersionId,
                modelVersion, status, resultSnapshot, List.of(), createdAt);
    }

    public static DiagnosisHistory reconstruct(
            UUID id, UUID farmerUserId, UUID cropTypeId, UUID modelId, UUID modelVersionId,
            String modelVersion, DiagnosisStatus status, String resultSnapshot, List<UUID> fileIds, Instant createdAt) {
        return new DiagnosisHistory(
                id, farmerUserId, cropTypeId, modelId, modelVersionId,
                modelVersion, status, resultSnapshot, fileIds, createdAt);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value;
    }

    private static List<UUID> normalizeFileIds(List<UUID> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) return List.of();
        if (fileIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("fileIds must not contain null");
        }
        return List.copyOf(new LinkedHashSet<>(fileIds));
    }

    public UUID getId() { return id; }
    public UUID getFarmerUserId() { return farmerUserId; }
    public UUID getCropTypeId() { return cropTypeId; }
    public UUID getModelId() { return modelId; }
    public UUID getModelVersionId() { return modelVersionId; }
    public String getModelVersion() { return modelVersion; }
    public DiagnosisStatus getStatus() { return status; }
    public String getResultSnapshot() { return resultSnapshot; }
    public List<UUID> getFileIds() { return fileIds; }
    public Instant getCreatedAt() { return createdAt; }
}
