package com.nongthinh.agri_catalog_service.domain.aimodel;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.AiModelStatus;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.AiModelTaskType;
import com.nongthinh.agri_catalog_service.domain.aimodel.valueobject.CropCoverageType;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;

public final class AiModel {

    private final UUID id;
    private final String code;
    private String name;
    private String description;
    private final AiModelTaskType taskType;
    private CropCoverageType cropCoverageType;
    private Set<UUID> cropTypeIds;
    private AiModelStatus status;
    private final Instant createdAt;
    private final UUID createdBy;
    private Instant updatedAt;
    private UUID updatedBy;
    private Instant deletedAt;
    private UUID deletedBy;

    private AiModel(
            UUID id,
            String code,
            String name,
            String description,
            AiModelTaskType taskType,
            CropCoverageType cropCoverageType,
            Set<UUID> cropTypeIds,
            AiModelStatus status,
            Instant createdAt,
            UUID createdBy,
            Instant updatedAt,
            UUID updatedBy,
            Instant deletedAt,
            UUID deletedBy) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.code = normalizeCode(code);
        this.name = Objects.requireNonNull(name, "name is required");
        this.description = description;
        this.taskType = Objects.requireNonNull(taskType, "taskType is required");
        this.cropCoverageType = Objects.requireNonNull(cropCoverageType, "cropCoverageType is required");
        this.cropTypeIds = immutableCropTypeIds(cropTypeIds);
        validateCropCoverage(this.cropCoverageType, this.cropTypeIds);
        this.status = Objects.requireNonNull(status, "status is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy is required");
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public static AiModel create(
            UUID id,
            String code,
            String name,
            String description,
            AiModelTaskType taskType,
            CropCoverageType cropCoverageType,
            Set<UUID> cropTypeIds,
            UUID createdBy,
            Instant now) {
        return new AiModel(
                id,
                code,
                name,
                description,
                taskType,
                cropCoverageType,
                cropTypeIds,
                AiModelStatus.DRAFT,
                now,
                createdBy,
                now,
                createdBy,
                null,
                null
        );
    }

    public static AiModel reconstruct(
            UUID id,
            String code,
            String name,
            String description,
            AiModelTaskType taskType,
            CropCoverageType cropCoverageType,
            Set<UUID> cropTypeIds,
            AiModelStatus status,
            Instant createdAt,
            UUID createdBy,
            Instant updatedAt,
            UUID updatedBy,
            Instant deletedAt,
            UUID deletedBy) {
        return new AiModel(
                id,
                code,
                name,
                description,
                taskType,
                cropCoverageType,
                cropTypeIds,
                status,
                createdAt,
                createdBy,
                updatedAt,
                updatedBy,
                deletedAt,
                deletedBy
        );
    }

    public void update(
            String name,
            String description,
            CropCoverageType cropCoverageType,
            Set<UUID> cropTypeIds,
            UUID updatedBy,
            Instant now) {
        ensureNotRetired();
        Set<UUID> updatedCropTypeIds = immutableCropTypeIds(cropTypeIds);
        validateCropCoverage(cropCoverageType, updatedCropTypeIds);
        this.name = Objects.requireNonNull(name, "name is required");
        this.description = description;
        this.cropCoverageType = Objects.requireNonNull(cropCoverageType, "cropCoverageType is required");
        this.cropTypeIds = updatedCropTypeIds;
        touch(now, updatedBy);
    }

    public void retire(UUID retiredBy, Instant now) {
        if (status == AiModelStatus.RETIRED) {
            return;
        }
        this.status = AiModelStatus.RETIRED;
        touch(now, retiredBy);
    }

    private static String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase(Locale.ROOT);
    }

    private static Set<UUID> immutableCropTypeIds(Set<UUID> cropTypeIds) {
        return cropTypeIds == null ? Set.of() : Set.copyOf(cropTypeIds);
    }

    private static void validateCropCoverage(CropCoverageType cropCoverageType, Set<UUID> cropTypeIds) {
        if ((cropCoverageType == CropCoverageType.SELECTED_CROPS && cropTypeIds.isEmpty())
                || (cropCoverageType == CropCoverageType.ALL_CROPS && !cropTypeIds.isEmpty())) {
            throw new BusinessException(ErrorCode.AI_MODEL_CROP_COVERAGE_INVALID);
        }
    }

    private void ensureNotRetired() {
        if (status == AiModelStatus.RETIRED) {
            throw new BusinessException(ErrorCode.AI_MODEL_RETIRED);
        }
    }

    private void touch(Instant now, UUID updatedBy) {
        this.updatedAt = Objects.requireNonNull(now, "now is required");
        this.updatedBy = Objects.requireNonNull(updatedBy, "updatedBy is required");
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public AiModelTaskType getTaskType() { return taskType; }
    public CropCoverageType getCropCoverageType() { return cropCoverageType; }
    public Set<UUID> getCropTypeIds() { return cropTypeIds; }
    public AiModelStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getUpdatedAt() { return updatedAt; }
    public UUID getUpdatedBy() { return updatedBy; }
    public Instant getDeletedAt() { return deletedAt; }
    public UUID getDeletedBy() { return deletedBy; }
}
