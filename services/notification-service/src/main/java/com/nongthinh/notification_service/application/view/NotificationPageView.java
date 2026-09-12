package com.nongthinh.notification_service.application.view;
import java.util.List;
public record NotificationPageView(List<NotificationView> items, int page, int size, boolean hasNext) {}
