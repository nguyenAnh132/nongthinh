package com.nongthinh.notification_service.application.port.in.notification;
import com.nongthinh.notification_service.application.view.NotificationStateView;
import java.util.UUID;
public interface MarkNotificationReadUseCase { NotificationStateView execute(UUID id); }
