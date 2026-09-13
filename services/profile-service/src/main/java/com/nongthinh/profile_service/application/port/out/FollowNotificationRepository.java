package com.nongthinh.profile_service.application.port.out;
import java.time.Instant;
import java.util.UUID;
public interface FollowNotificationRepository {
    void newFollower(UUID eventId, UUID actor, UUID recipient, Instant now);
    void publishedPost(UUID postId, UUID author, Instant occurredAt);
}
