package com.nongthinh.post_service.application.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record PostEngagementEvent(UUID eventId, String eventType, int schemaVersion,
        Instant occurredAt, UUID postId, long version, Map<String, Object> payload,
        UUID actorUserId, UUID recipientUserId, String notificationType,
        boolean publicEngagement) {
}
