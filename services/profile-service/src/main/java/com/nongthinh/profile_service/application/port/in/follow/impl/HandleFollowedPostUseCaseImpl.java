package com.nongthinh.profile_service.application.port.in.follow.impl;
import com.nongthinh.profile_service.application.event.PostPublishedEvent;
import com.nongthinh.profile_service.application.port.in.follow.HandleFollowedPostUseCase;
import com.nongthinh.profile_service.application.port.out.FollowNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class HandleFollowedPostUseCaseImpl implements HandleFollowedPostUseCase {
    private final FollowNotificationRepository notifications;
    @Transactional
    public void execute(PostPublishedEvent event) {
        if (event == null) throw new IllegalArgumentException("Missing post event");
        if (!"post.published".equals(event.eventType())) return;
        if (event.schemaVersion() != 1 || event.eventId() == null || event.postId() == null
                || event.actorUserId() == null || event.occurredAt() == null)
            throw new IllegalArgumentException("Invalid published post event");
        if (event.publicEngagement())
            notifications.publishedPost(event.postId(), event.actorUserId(), event.occurredAt());
    }
}
