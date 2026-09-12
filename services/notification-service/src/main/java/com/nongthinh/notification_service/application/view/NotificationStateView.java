package com.nongthinh.notification_service.application.view;
import java.util.UUID;
public record NotificationStateView(UUID userId, long version, long unreadCount, UUID eventId, String eventType) {}
