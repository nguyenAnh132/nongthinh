package com.nongthinh.brand_service.infra.client.profileservice;

import java.util.UUID;

public record UpdateBrandStatusParam(
    String status,
    String rejectionReason,
    UUID actorUserId
) {
}
