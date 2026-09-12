package com.nongthinh.brand_service.domain.brandapprovalprocess;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;

@Getter
public class BrandApprovalProcess {

    private final UUID id;
    private final UUID brandProfileId;
    private final String camundaProcessInstanceId;
    private final String camundaBusinessKey;
    private final BrandApprovalProcessStatus status;
    private final UUID assignedReviewerId;
    private final Instant startedAt;
    private final Instant completedAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    private BrandApprovalProcess(
            UUID id,
            UUID brandProfileId,
            String camundaProcessInstanceId,
            String camundaBusinessKey,
            BrandApprovalProcessStatus status,
            UUID assignedReviewerId,
            Instant startedAt,
            Instant completedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.brandProfileId = brandProfileId;
        this.camundaProcessInstanceId = camundaProcessInstanceId;
        this.camundaBusinessKey = camundaBusinessKey;
        this.status = status;
        this.assignedReviewerId = assignedReviewerId;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static BrandApprovalProcess start(
            UUID id,
            UUID brandProfileId,
            String camundaProcessInstanceId,
            String camundaBusinessKey,
            Instant now
    ) {
        return new BrandApprovalProcess(
                id,
                brandProfileId,
                camundaProcessInstanceId,
                camundaBusinessKey,
                BrandApprovalProcessStatus.STARTED,
                null,
                now,
                null,
                now,
                now
        );
    }

    public static BrandApprovalProcess reconstruct(
            UUID id,
            UUID brandProfileId,
            String camundaProcessInstanceId,
            String camundaBusinessKey,
            BrandApprovalProcessStatus status,
            UUID assignedReviewerId,
            Instant startedAt,
            Instant completedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new BrandApprovalProcess(
                id,
                brandProfileId,
                camundaProcessInstanceId,
                camundaBusinessKey,
                status,
                assignedReviewerId,
                startedAt,
                completedAt,
                createdAt,
                updatedAt
        );
    }

    public BrandApprovalProcess complete(UUID reviewerId, Instant now) {
        return new BrandApprovalProcess(
                id,
                brandProfileId,
                camundaProcessInstanceId,
                camundaBusinessKey,
                BrandApprovalProcessStatus.COMPLETED,
                reviewerId != null ? reviewerId : assignedReviewerId,
                startedAt,
                now,
                createdAt,
                now
        );
    }

    public BrandApprovalProcess cancel(UUID reviewerId, Instant now) {
        return new BrandApprovalProcess(
                id,
                brandProfileId,
                camundaProcessInstanceId,
                camundaBusinessKey,
                BrandApprovalProcessStatus.CANCELLED,
                reviewerId != null ? reviewerId : assignedReviewerId,
                startedAt,
                now,
                createdAt,
                now
        );
    }
}
