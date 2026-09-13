package com.nongthinh.notification_service.application.port.out.repository;
import com.nongthinh.notification_service.application.event.PostEngagementEvent;
import com.nongthinh.notification_service.application.view.*;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
public interface NotificationRepository {
    boolean claimEvent(UUID eventId, Instant now);
    boolean createOrCoalesce(PostEngagementEvent event, UUID id);
    boolean createSocial(com.nongthinh.notification_service.application.event.SocialNotificationEvent event, UUID id);
    NotificationPageView list(UUID userId, int page, int size);
    NotificationStateView state(UUID userId);
    List<NotificationStateView> states(Collection<UUID> users);
    boolean markRead(UUID userId, UUID id, Instant now);
    boolean markAllRead(UUID userId, Instant now);
    void changed(UUID userId, UUID eventId, String eventType);
}
