package com.nongthinh.notification_service.presentation.controller;

import com.nongthinh.notification_service.application.port.in.notification.*;
import com.nongthinh.notification_service.application.view.*;
import com.nongthinh.notification_service.common.response.ApiResponse;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "in-app-notification.enabled", havingValue = "true")
public class NotificationController {
    private final ListNotificationsUseCase list;
    private final GetUnreadNotificationCountUseCase unread;
    private final MarkNotificationReadUseCase read;
    private final MarkAllNotificationsReadUseCase readAll;

    @GetMapping
    public ApiResponse<NotificationPageView> list(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return response(list.execute(page, size));
    }
    @GetMapping("/unread-count")
    public ApiResponse<NotificationStateView> unread() { return response(unread.execute()); }
    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationStateView> read(@PathVariable UUID id) { return response(read.execute(id)); }
    @PatchMapping("/read-all")
    public ApiResponse<NotificationStateView> readAll() { return response(readAll.execute()); }

    private <T> ApiResponse<T> response(T result) {
        return ApiResponse.<T>builder().message("Notification request completed successfully")
                .result(Optional.of(result)).build();
    }
}
