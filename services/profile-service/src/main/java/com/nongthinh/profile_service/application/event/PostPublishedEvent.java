package com.nongthinh.profile_service.application.event;
import java.time.Instant;
import java.util.UUID;
public record PostPublishedEvent(UUID eventId, String eventType, int schemaVersion, Instant occurredAt,
        UUID postId, UUID actorUserId, boolean publicEngagement) {}
