package com.nongthinh.agri_catalog_service.domain.aimodelversion;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodelversion.valueobject.AiModelVersionStatus;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;

public final class AiModelVersion {

    private final UUID id;
    private final UUID modelId;
    private final String version;
    private final UUID artifactFileId;
    private final String artifactSha256;
    private final int inputWidth;
    private final int inputHeight;
    private final List<AiModelVersionClass> classes;
    private AiModelVersionStatus status;
    private String validationReport;
    private final Instant createdAt;
    private final UUID createdBy;
    private Instant validatedAt;
    private UUID validatedBy;
    private Instant retiredAt;
    private UUID retiredBy;

    private AiModelVersion(
            UUID id,
            UUID modelId,
            String version,
            UUID artifactFileId,
            String artifactSha256,
            int inputWidth,
            int inputHeight,
            List<AiModelVersionClass> classes,
            AiModelVersionStatus status,
            String validationReport,
            Instant createdAt,
            UUID createdBy,
            Instant validatedAt,
            UUID validatedBy,
            Instant retiredAt,
            UUID retiredBy) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.modelId = Objects.requireNonNull(modelId, "modelId is required");
        this.version = requireTrimmed(version, "version");
        this.artifactFileId = Objects.requireNonNull(artifactFileId, "artifactFileId is required");
        this.artifactSha256 = normalizeSha256(artifactSha256);
        if (inputWidth <= 0 || inputHeight <= 0) {
            throw new IllegalArgumentException("input dimensions must be positive");
        }
        this.inputWidth = inputWidth;
        this.inputHeight = inputHeight;
        this.classes = immutableManifest(id, classes);
        this.status = Objects.requireNonNull(status, "status is required");
        this.validationReport = validationReport;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy is required");
        this.validatedAt = validatedAt;
        this.validatedBy = validatedBy;
        this.retiredAt = retiredAt;
        this.retiredBy = retiredBy;
    }

    public static AiModelVersion create(
            UUID id,
            UUID modelId,
            String version,
            UUID artifactFileId,
            String artifactSha256,
            int inputWidth,
            int inputHeight,
            List<AiModelVersionClass> classes,
            UUID createdBy,
            Instant now) {
        return new AiModelVersion(
                id, modelId, version, artifactFileId, artifactSha256, inputWidth, inputHeight, classes,
                AiModelVersionStatus.DRAFT, null, now, createdBy, null, null, null, null);
    }

    public static AiModelVersion reconstruct(
            UUID id,
            UUID modelId,
            String version,
            UUID artifactFileId,
            String artifactSha256,
            int inputWidth,
            int inputHeight,
            List<AiModelVersionClass> classes,
            AiModelVersionStatus status,
            String validationReport,
            Instant createdAt,
            UUID createdBy,
            Instant validatedAt,
            UUID validatedBy,
            Instant retiredAt,
            UUID retiredBy) {
        return new AiModelVersion(
                id, modelId, version, artifactFileId, artifactSha256, inputWidth, inputHeight, classes,
                status, validationReport, createdAt, createdBy, validatedAt, validatedBy, retiredAt, retiredBy);
    }

    public void startValidation() {
        requireStatus(AiModelVersionStatus.DRAFT);
        status = AiModelVersionStatus.VALIDATING;
        validationReport = null;
    }

    public void completeValidation(boolean successful, String report, UUID validatorId, Instant now) {
        requireStatus(AiModelVersionStatus.VALIDATING);
        this.validationReport = report;
        if (successful) {
            status = AiModelVersionStatus.VALIDATED;
            validatedAt = Objects.requireNonNull(now, "now is required");
            validatedBy = Objects.requireNonNull(validatorId, "validatorId is required");
            return;
        }
        status = AiModelVersionStatus.DRAFT;
        validatedAt = null;
        validatedBy = null;
    }

    public void markReady() {
        requireStatus(AiModelVersionStatus.VALIDATED);
        status = AiModelVersionStatus.READY;
    }

    public void activate() {
        if (status == AiModelVersionStatus.ACTIVE) {
            return;
        }
        requireStatus(AiModelVersionStatus.READY);
        status = AiModelVersionStatus.ACTIVE;
    }

    public void deactivate() {
        if (status == AiModelVersionStatus.ACTIVE) {
            status = AiModelVersionStatus.READY;
        }
    }

    public void retire(UUID actorId, Instant now) {
        if (status == AiModelVersionStatus.RETIRED) {
            return;
        }
        if (status == AiModelVersionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.AI_MODEL_VERSION_STATE_INVALID);
        }
        status = AiModelVersionStatus.RETIRED;
        retiredAt = Objects.requireNonNull(now, "now is required");
        retiredBy = Objects.requireNonNull(actorId, "actorId is required");
    }

    private void requireStatus(AiModelVersionStatus expected) {
        if (status != expected) {
            throw new BusinessException(ErrorCode.AI_MODEL_VERSION_STATE_INVALID);
        }
    }

    private static List<AiModelVersionClass> immutableManifest(UUID versionId, List<AiModelVersionClass> classes) {
        if (classes == null || classes.isEmpty()) {
            throw new BusinessException(ErrorCode.AI_MODEL_VERSION_MANIFEST_INVALID);
        }
        Set<Integer> classIndexes = new HashSet<>();
        Set<String> classCodes = new HashSet<>();
        for (AiModelVersionClass item : classes) {
            if (item == null
                    || !versionId.equals(item.getModelVersionId())
                    || !classIndexes.add(item.getClassIndex())
                    || !classCodes.add(item.getClassCode())) {
                throw new BusinessException(ErrorCode.AI_MODEL_VERSION_MANIFEST_INVALID);
            }
        }
        return List.copyOf(classes);
    }

    private static String requireTrimmed(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static String normalizeSha256(String value) {
        if (value == null || !value.matches("(?i)^[a-f0-9]{64}$")) {
            throw new IllegalArgumentException("artifactSha256 must be a SHA-256 hex string");
        }
        return value.toLowerCase(Locale.ROOT);
    }

    public UUID getId() { return id; }
    public UUID getModelId() { return modelId; }
    public String getVersion() { return version; }
    public UUID getArtifactFileId() { return artifactFileId; }
    public String getArtifactSha256() { return artifactSha256; }
    public int getInputWidth() { return inputWidth; }
    public int getInputHeight() { return inputHeight; }
    public List<AiModelVersionClass> getClasses() { return classes; }
    public AiModelVersionStatus getStatus() { return status; }
    public String getValidationReport() { return validationReport; }
    public Instant getCreatedAt() { return createdAt; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getValidatedAt() { return validatedAt; }
    public UUID getValidatedBy() { return validatedBy; }
    public Instant getRetiredAt() { return retiredAt; }
    public UUID getRetiredBy() { return retiredBy; }
}
