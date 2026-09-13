package com.nongthinh.notification_service.application.event;
import java.time.Instant;
import java.util.UUID;
public record SocialNotificationEvent(UUID eventId, int schemaVersion, Instant occurredAt,
        UUID actorUserId, UUID recipientUserId, String notificationType, UUID entityId) {}
