package com.nongthinh.notification_service.presentation.controller;

import com.nongthinh.notification_service.application.port.out.CurrentUserProvider;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import com.nongthinh.notification_service.infra.realtime.SseConnectionRegistry;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import com.nongthinh.notification_service.common.response.ApiResponse;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class RealtimeController {
    private final boolean notificationsEnabled;
    private final boolean postsEnabled;
    private final SseConnectionRegistry registry;
    private final CurrentUserProvider currentUser;

    public RealtimeController(SseConnectionRegistry registry, CurrentUserProvider currentUser,
            @Value("${in-app-notification.enabled:false}") boolean notificationsEnabled,
            @Value("${post-realtime.enabled:false}") boolean postsEnabled) {
        this.registry = registry;
        this.currentUser = currentUser;
        this.notificationsEnabled = notificationsEnabled;
        this.postsEnabled = postsEnabled;
    }

    @GetMapping("/events/config")
    public ApiResponse<Map<String, Boolean>> config() {
        currentUser.getCurrentUserId();
        return ApiResponse.<Map<String, Boolean>>builder()
                .result(Optional.of(Map.of("notifications", notificationsEnabled, "postEngagement", postsEnabled)))
                .build();
    }

    @GetMapping(value = "/events/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> stream(@RequestParam(defaultValue = "notifications") String channels,
                                             @AuthenticationPrincipal Jwt jwt) {
        Set<String> requested = Arrays.stream(channels.split(",")).map(String::trim).collect(Collectors.toSet());
        if (requested.isEmpty() || !Set.of("notifications", "post-engagement").containsAll(requested)
                || (requested.contains("notifications") && !notificationsEnabled)
                || (requested.contains("post-engagement") && !postsEnabled))
            throw new BusinessException(ErrorCode.NOTIFICATION_REQUEST_INVALID);
        var userId = currentUser.getCurrentUserId();
        return ResponseEntity.ok().header("Cache-Control", "no-cache, no-transform")
                .header("X-Accel-Buffering", "no")
                .body(registry.connect(userId, requested, jwt.getExpiresAt()));
    }
}
