package com.nongthinh.notification_service.application.port.in.notification.impl;
import com.nongthinh.notification_service.application.port.in.notification.GetUnreadNotificationCountUseCase;
import com.nongthinh.notification_service.application.port.out.CurrentUserProvider;
import com.nongthinh.notification_service.application.port.out.repository.NotificationRepository;
import com.nongthinh.notification_service.application.view.NotificationStateView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class GetUnreadNotificationCountUseCaseImpl implements GetUnreadNotificationCountUseCase {
    private final NotificationRepository repository;
    private final CurrentUserProvider currentUser;
    public NotificationStateView execute() { return repository.state(currentUser.getCurrentUserId()); }
}
