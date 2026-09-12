package com.nongthinh.notification_service.application.port.in.notification;
import com.nongthinh.notification_service.application.view.NotificationPageView;
public interface ListNotificationsUseCase { NotificationPageView execute(int page, int size); }
