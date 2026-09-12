package com.nongthinh.notification_service.application.view;
import java.time.Instant;
import java.util.UUID;
public record NotificationView(UUID id, UUID actorUserId, String type, String entityType,
        UUID entityId, Instant readAt, Instant createdAt) {}
