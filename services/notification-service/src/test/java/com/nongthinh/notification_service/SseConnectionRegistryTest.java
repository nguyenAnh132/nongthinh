package com.nongthinh.notification_service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import com.nongthinh.notification_service.application.event.PostEngagementEvent;
import com.nongthinh.notification_service.application.port.out.repository.NotificationRepository;
import com.nongthinh.notification_service.infra.realtime.SseConnectionRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

class SseConnectionRegistryTest {
    @Test
    void supportsMultipleTabsAndCleansUpEveryConnectionOnShutdown() {
        var meters = new SimpleMeterRegistry();
        try (var executor = Executors.newSingleThreadExecutor()) {
            var registry = new SseConnectionRegistry(mock(NotificationRepository.class), executor, meters, true, true);
            UUID user = UUID.fromString("00000000-0000-0000-0000-000000000001");
            registry.connect(user, Set.of("notifications"), Instant.now().plusSeconds(60));
            registry.connect(user, Set.of("notifications", "post-engagement"), Instant.now().plusSeconds(60));
            assertThat(meters.get("sse.connections.active").gauge().value()).isEqualTo(2);
            registry.heartbeat();
            registry.shutdown();
            assertThat(meters.get("sse.connections.active").gauge().value()).isZero();
        }
    }

    @Test
    void revocationClosesAllTabsForOnlyTheAffectedAccount() {
        var meters = new SimpleMeterRegistry();
        try (var executor = Executors.newSingleThreadExecutor()) {
            var registry = new SseConnectionRegistry(mock(NotificationRepository.class), executor, meters, true, true);
            UUID user = UUID.fromString("00000000-0000-0000-0000-000000000001");
            UUID other = UUID.fromString("00000000-0000-0000-0000-000000000002");
            registry.connect(user, Set.of("notifications"), null);
            registry.connect(user, Set.of("notifications", "post-engagement"), null);
            registry.connect(other, Set.of("notifications"), null);
            registry.disconnect(user);
            assertThat(meters.get("sse.connections.active").gauge().value()).isEqualTo(1);
            registry.disconnect(user);
            assertThat(meters.get("sse.connections.active").gauge().value()).isEqualTo(1);
            registry.shutdown();
        }
    }

    @Test
    void rejectsExpiredSessions() {
        var meters = new SimpleMeterRegistry();
        try (var executor = Executors.newSingleThreadExecutor()) {
            var registry = new SseConnectionRegistry(mock(NotificationRepository.class), executor, meters, true, true);
            org.assertj.core.api.Assertions.assertThatThrownBy(() -> registry.connect(UUID.randomUUID(),
                    Set.of("notifications"), Instant.now().minusSeconds(1)))
                    .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
            assertThat(meters.get("sse.connections.active").gauge().value()).isZero();
        }
    }
}
