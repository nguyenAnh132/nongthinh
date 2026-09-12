package com.nongthinh.profile_service.application.view;

import java.time.Instant;
import java.util.UUID;
import com.nongthinh.profile_service.domain.brandlifecyclelog.BrandLifecycleLog;

public record BrandLifecycleLogView(
        UUID id,
        UUID brandProfileId,
        String action,
        UUID actorUserId,
        String fromStatus,
        String toStatus,
        String payloadJson,
        Instant createdAt
) {
    public static BrandLifecycleLogView from(BrandLifecycleLog log) {
        return new BrandLifecycleLogView(
                log.getId(),
                log.getBrandProfileId(),
                log.getAction().getValue(),
                log.getActorUserId(),
                log.getFromStatus() != null ? log.getFromStatus().getValue() : null,
                log.getToStatus() != null ? log.getToStatus().getValue() : null,
                log.getPayloadJson(),
                log.getCreatedAt()
        );
    }
}
