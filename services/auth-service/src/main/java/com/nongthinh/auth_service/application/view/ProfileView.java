package com.nongthinh.auth_service.application.view;

import java.time.Instant;
import java.util.UUID;

public record ProfileView(
    UUID profileId,
    String type,
    String displayName,
    String avatarUrl,
    String bannerUrl,
    String status,
    String rejectionReason,
    Instant scheduledDeletionAt
) {

}
