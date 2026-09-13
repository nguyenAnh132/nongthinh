package com.nongthinh.notification_service.application.port.in.notification.impl;
import com.nongthinh.notification_service.application.event.SocialNotificationEvent;
import com.nongthinh.notification_service.application.port.in.notification.HandleSocialNotificationUseCase;
import com.nongthinh.notification_service.application.port.out.*;
import com.nongthinh.notification_service.application.port.out.repository.NotificationRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class HandleSocialNotificationUseCaseImpl implements HandleSocialNotificationUseCase {
    private final NotificationRepository repository;
    private final IdGenerator ids;
    private final ClockProvider clock;
    @Transactional
    public void execute(SocialNotificationEvent event) {
        if (event == null || event.schemaVersion() != 1 || event.eventId() == null
                || event.occurredAt() == null || event.actorUserId() == null || event.recipientUserId() == null
                || event.entityId() == null || event.notificationType() == null
                || !Set.of("NEW_FOLLOWER", "FOLLOWED_USER_POST").contains(event.notificationType()))
            throw new IllegalArgumentException("Invalid social notification event");
        if (event.actorUserId().equals(event.recipientUserId())) return;
        if (!repository.claimEvent(event.eventId(), clock.now())) return;
        if (repository.createSocial(event, ids.generate()))
            repository.changed(event.recipientUserId(), ids.generate(), "notification.created");
    }
}
