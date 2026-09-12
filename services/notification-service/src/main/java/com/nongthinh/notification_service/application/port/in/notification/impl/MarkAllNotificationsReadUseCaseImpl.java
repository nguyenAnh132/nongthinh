package com.nongthinh.notification_service.application.port.in.notification.impl;
import com.nongthinh.notification_service.application.port.in.notification.MarkAllNotificationsReadUseCase;
import com.nongthinh.notification_service.application.port.out.*;
import com.nongthinh.notification_service.application.port.out.repository.NotificationRepository;
import com.nongthinh.notification_service.application.view.NotificationStateView;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class MarkAllNotificationsReadUseCaseImpl implements MarkAllNotificationsReadUseCase {
    private final NotificationRepository repository;
    private final CurrentUserProvider currentUser;
    private final IdGenerator ids;
    private final ClockProvider clock;
    @Transactional
    public NotificationStateView execute() {
        UUID userId = currentUser.getCurrentUserId();
        if (repository.markAllRead(userId, clock.now())) {
            repository.changed(userId, ids.generate(), "notification.read");
        }
        return repository.state(userId);
    }
}
