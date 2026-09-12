package com.nongthinh.notification_service.application.port.in.notification.impl;
import com.nongthinh.notification_service.application.event.PostEngagementEvent;
import com.nongthinh.notification_service.application.port.in.notification.HandlePostEngagementUseCase;
import com.nongthinh.notification_service.application.port.out.*;
import com.nongthinh.notification_service.application.port.out.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class HandlePostEngagementUseCaseImpl implements HandlePostEngagementUseCase {
    private final NotificationRepository repository;
    private final IdGenerator ids;
    private final ClockProvider clock;
    @Transactional
    public void execute(PostEngagementEvent event) {
        if (!repository.claimEvent(event.eventId(), clock.now())) return;
        if (event.notificationType() == null || event.recipientUserId() == null
                || event.recipientUserId().equals(event.actorUserId())) return;
        if (repository.createOrCoalesce(event, ids.generate())) {
            repository.changed(event.recipientUserId(), ids.generate(), "notification.created");
        }
    }
}
