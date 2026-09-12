package com.nongthinh.notification_service.application.port.in.notification.impl;
import com.nongthinh.notification_service.application.port.in.notification.ListNotificationsUseCase;
import com.nongthinh.notification_service.application.port.out.CurrentUserProvider;
import com.nongthinh.notification_service.application.port.out.repository.NotificationRepository;
import com.nongthinh.notification_service.application.view.NotificationPageView;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class ListNotificationsUseCaseImpl implements ListNotificationsUseCase {
    private final NotificationRepository repository;
    private final CurrentUserProvider currentUser;
    public NotificationPageView execute(int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new BusinessException(ErrorCode.NOTIFICATION_REQUEST_INVALID);
        return repository.list(currentUser.getCurrentUserId(), page, size);
    }
}
