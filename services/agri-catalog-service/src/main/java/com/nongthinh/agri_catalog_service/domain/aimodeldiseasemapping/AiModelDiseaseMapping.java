package com.nongthinh.agri_catalog_service.domain.aimodeldiseasemapping;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class AiModelDiseaseMapping {

    private final UUID id;
    private UUID modelVersionClassId;
    private UUID cropTypeId;
    private UUID diseaseId;
    private final Instant createdAt;
    private final UUID createdBy;
    private Instant updatedAt;
    private UUID updatedBy;

    private AiModelDiseaseMapping(
            UUID id,
            UUID modelVersionClassId,
            UUID cropTypeId,
            UUID diseaseId,
            Instant createdAt,
            UUID createdBy,
            Instant updatedAt,
            UUID updatedBy) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.modelVersionClassId = Objects.requireNonNull(modelVersionClassId, "modelVersionClassId is required");
        this.cropTypeId = Objects.requireNonNull(cropTypeId, "cropTypeId is required");
        this.diseaseId = Objects.requireNonNull(diseaseId, "diseaseId is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy is required");
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public static AiModelDiseaseMapping create(
            UUID id,
            UUID modelVersionClassId,
            UUID cropTypeId,
            UUID diseaseId,
            UUID createdBy,
            Instant now) {
        return new AiModelDiseaseMapping(
                id, modelVersionClassId, cropTypeId, diseaseId, now, createdBy, null, null);
    }

    public static AiModelDiseaseMapping reconstruct(
            UUID id,
            UUID modelVersionClassId,
            UUID cropTypeId,
            UUID diseaseId,
            Instant createdAt,
            UUID createdBy,
            Instant updatedAt,
            UUID updatedBy) {
        return new AiModelDiseaseMapping(
                id, modelVersionClassId, cropTypeId, diseaseId, createdAt, createdBy, updatedAt, updatedBy);
    }

    public void update(
            UUID modelVersionClassId,
            UUID cropTypeId,
            UUID diseaseId,
            UUID actorId,
            Instant now) {
        this.modelVersionClassId = Objects.requireNonNull(modelVersionClassId, "modelVersionClassId is required");
        this.cropTypeId = Objects.requireNonNull(cropTypeId, "cropTypeId is required");
        this.diseaseId = Objects.requireNonNull(diseaseId, "diseaseId is required");
        this.updatedAt = Objects.requireNonNull(now, "now is required");
        this.updatedBy = Objects.requireNonNull(actorId, "actorId is required");
    }

    public UUID getId() { return id; }
    public UUID getModelVersionClassId() { return modelVersionClassId; }
    public UUID getCropTypeId() { return cropTypeId; }
    public UUID getDiseaseId() { return diseaseId; }
    public Instant getCreatedAt() { return createdAt; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getUpdatedAt() { return updatedAt; }
    public UUID getUpdatedBy() { return updatedBy; }
}
