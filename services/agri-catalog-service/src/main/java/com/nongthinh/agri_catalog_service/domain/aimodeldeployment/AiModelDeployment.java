package com.nongthinh.agri_catalog_service.domain.aimodeldeployment;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import com.nongthinh.agri_catalog_service.common.exception.ErrorCode;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.valueobject.AiModelDeploymentScope;
import com.nongthinh.agri_catalog_service.domain.aimodeldeployment.valueobject.AiModelDeploymentStatus;
import com.nongthinh.agri_catalog_service.domain.exception.BusinessException;

public final class AiModelDeployment {

    private final UUID id;
    private final UUID modelVersionId;
    private final UUID cropTypeId;
    private final AiModelDeploymentScope deploymentScope;
    private final int priority;
    private AiModelDeploymentStatus status;
    private final Instant createdAt;
    private final UUID createdBy;
    private Instant activatedAt;
    private UUID activatedBy;

    private AiModelDeployment(
            UUID id,
            UUID modelVersionId,
            UUID cropTypeId,
            AiModelDeploymentScope deploymentScope,
            int priority,
            AiModelDeploymentStatus status,
            Instant createdAt,
            UUID createdBy,
            Instant activatedAt,
            UUID activatedBy) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.modelVersionId = Objects.requireNonNull(modelVersionId, "modelVersionId is required");
        this.cropTypeId = cropTypeId;
        this.deploymentScope = Objects.requireNonNull(deploymentScope, "deploymentScope is required");
        if (priority < 0) {
            throw new IllegalArgumentException("priority must not be negative");
        }
        this.priority = priority;
        validateScope();
        this.status = Objects.requireNonNull(status, "status is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.createdBy = Objects.requireNonNull(createdBy, "createdBy is required");
        this.activatedAt = activatedAt;
        this.activatedBy = activatedBy;
    }

    public static AiModelDeployment create(
            UUID id,
            UUID modelVersionId,
            UUID cropTypeId,
            AiModelDeploymentScope deploymentScope,
            int priority,
            UUID createdBy,
            Instant now) {
        return new AiModelDeployment(
                id, modelVersionId, cropTypeId, deploymentScope, priority,
                AiModelDeploymentStatus.INACTIVE, now, createdBy, null, null);
    }

    public static AiModelDeployment reconstruct(
            UUID id,
            UUID modelVersionId,
            UUID cropTypeId,
            AiModelDeploymentScope deploymentScope,
            int priority,
            AiModelDeploymentStatus status,
            Instant createdAt,
            UUID createdBy,
            Instant activatedAt,
            UUID activatedBy) {
        return new AiModelDeployment(
                id, modelVersionId, cropTypeId, deploymentScope, priority, status,
                createdAt, createdBy, activatedAt, activatedBy);
    }

    public void activate(UUID actorId, Instant now) {
        if (status == AiModelDeploymentStatus.ACTIVE) {
            return;
        }
        if (status != AiModelDeploymentStatus.INACTIVE) {
            throw new BusinessException(ErrorCode.AI_MODEL_DEPLOYMENT_STATE_INVALID);
        }
        status = AiModelDeploymentStatus.ACTIVE;
        activatedAt = Objects.requireNonNull(now, "now is required");
        activatedBy = Objects.requireNonNull(actorId, "actorId is required");
    }

    public void deactivate() {
        if (status == AiModelDeploymentStatus.ACTIVE) {
            status = AiModelDeploymentStatus.INACTIVE;
            return;
        }
        if (status != AiModelDeploymentStatus.INACTIVE) {
            throw new BusinessException(ErrorCode.AI_MODEL_DEPLOYMENT_STATE_INVALID);
        }
    }

    private void validateScope() {
        boolean valid = (deploymentScope == AiModelDeploymentScope.CROP && cropTypeId != null)
                || (deploymentScope == AiModelDeploymentScope.ALL_CROPS && cropTypeId == null);
        if (!valid) {
            throw new BusinessException(ErrorCode.AI_MODEL_DEPLOYMENT_SCOPE_INVALID);
        }
    }

    public UUID getId() { return id; }
    public UUID getModelVersionId() { return modelVersionId; }
    public UUID getCropTypeId() { return cropTypeId; }
    public AiModelDeploymentScope getDeploymentScope() { return deploymentScope; }
    public int getPriority() { return priority; }
    public AiModelDeploymentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getActivatedAt() { return activatedAt; }
    public UUID getActivatedBy() { return activatedBy; }
}
