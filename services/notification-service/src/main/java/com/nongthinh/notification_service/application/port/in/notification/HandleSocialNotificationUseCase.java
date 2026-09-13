package com.nongthinh.notification_service.application.port.in.notification;
import com.nongthinh.notification_service.application.event.SocialNotificationEvent;
public interface HandleSocialNotificationUseCase { void execute(SocialNotificationEvent event); }
