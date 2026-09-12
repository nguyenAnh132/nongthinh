package com.nongthinh.profile_service.domain.brandlifecyclelog;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import com.nongthinh.profile_service.domain.brandlifecyclelog.valueobject.BrandLifecycleAction;
import com.nongthinh.profile_service.domain.brandprofile.valueobject.BrandProfileStatus;

public final class BrandLifecycleLog {

    private final UUID id;
    private final UUID brandProfileId;
    private final BrandLifecycleAction action;
    private final UUID actorUserId;
    private final BrandProfileStatus fromStatus;
    private final BrandProfileStatus toStatus;
    private final String payloadJson;
    private final Instant createdAt;

    private BrandLifecycleLog(
        UUID id,
        UUID brandProfileId,
        BrandLifecycleAction action,
        UUID actorUserId,
        BrandProfileStatus fromStatus,
        BrandProfileStatus toStatus,
        String payloadJson,
        Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.brandProfileId = Objects.requireNonNull(brandProfileId, "brandProfileId is required");
        this.action = Objects.requireNonNull(action, "action is required");
        this.actorUserId = actorUserId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.payloadJson = payloadJson;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
    }

    public static BrandLifecycleLog recordStatusChange(
        UUID id,
        UUID brandProfileId,
        UUID actorUserId,
        BrandProfileStatus fromStatus,
        BrandProfileStatus toStatus,
        String payloadJson,
        Instant createdAt
    ) {
        return new BrandLifecycleLog(
            id,
            brandProfileId,
            BrandLifecycleAction.STATUS_CHANGED,
            actorUserId,
            fromStatus,
            toStatus,
            payloadJson,
            createdAt
        );
    }

    public static BrandLifecycleLog record(
        UUID id,
        UUID brandProfileId,
        BrandLifecycleAction action,
        UUID actorUserId,
        BrandProfileStatus fromStatus,
        BrandProfileStatus toStatus,
        String payloadJson,
        Instant createdAt
    ) {
        return new BrandLifecycleLog(
            id,
            brandProfileId,
            action,
            actorUserId,
            fromStatus,
            toStatus,
            payloadJson,
            createdAt
        );
    }

    public static BrandLifecycleLog reconstruct(
        UUID id,
        UUID brandProfileId,
        BrandLifecycleAction action,
        UUID actorUserId,
        BrandProfileStatus fromStatus,
        BrandProfileStatus toStatus,
        String payloadJson,
        Instant createdAt
    ) {
        return new BrandLifecycleLog(
            id,
            brandProfileId,
            action,
            actorUserId,
            fromStatus,
            toStatus,
            payloadJson,
            createdAt
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getBrandProfileId() {
        return brandProfileId;
    }

    public BrandLifecycleAction getAction() {
        return action;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public BrandProfileStatus getFromStatus() {
        return fromStatus;
    }

    public BrandProfileStatus getToStatus() {
        return toStatus;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
