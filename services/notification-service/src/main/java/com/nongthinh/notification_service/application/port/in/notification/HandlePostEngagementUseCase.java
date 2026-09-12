package com.nongthinh.notification_service.application.port.in.notification;
import com.nongthinh.notification_service.application.event.PostEngagementEvent;
public interface HandlePostEngagementUseCase { void execute(PostEngagementEvent event); }
