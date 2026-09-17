package com.nongthinh.notification_service.infra.realtime;

import com.nongthinh.notification_service.application.event.PostEngagementEvent;
import com.nongthinh.notification_service.application.port.out.repository.NotificationRepository;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseConnectionRegistry {
    private final ConcurrentMap<SseEmitter, Connection> connections = new ConcurrentHashMap<>();
    private final NotificationRepository notifications;
    private final ExecutorService executor;
    private final MeterRegistry meters;
    private final boolean notificationsEnabled;
    private final boolean postsEnabled;

    public SseConnectionRegistry(NotificationRepository notifications,
            @Qualifier("sseExecutor") ExecutorService executor, MeterRegistry meters,
            @Value("${in-app-notification.enabled:false}") boolean notificationsEnabled,
            @Value("${post-realtime.enabled:false}") boolean postsEnabled) {
        this.notifications = notifications;
        this.executor = executor;
        this.meters = meters;
        this.notificationsEnabled = notificationsEnabled;
        this.postsEnabled = postsEnabled;
        meters.gauge("sse.connections.active", connections, Map::size);
    }

    public synchronized SseEmitter connect(UUID userId, Set<String> channels, Instant expiresAt) {
        if (connections.size() >= 10000 || connections.values().stream().filter(c -> c.userId.equals(userId)).count() >= 10)
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS);
        long lifetime = expiresAt == null ? 1800000 : Math.min(1800000, expiresAt.toEpochMilli() - System.currentTimeMillis());
        if (lifetime <= 0) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        SseEmitter emitter = new SseEmitter(lifetime);
        Connection connection = new Connection(userId, emitter, Set.copyOf(channels));
        connections.put(emitter, connection);
        emitter.onCompletion(() -> connections.remove(emitter));
        emitter.onTimeout(() -> close(connection));
        emitter.onError(error -> close(connection));
        meters.counter("sse.connections.opened").increment();
        send(connection, SseEmitter.event().name("connected").data(Map.of("retry", 3000)).reconnectTime(3000));
        return emitter;
    }

    public void broadcast(PostEngagementEvent event) {
        if (!postsEnabled || !event.publicEngagement()) return;
        // Public streams never contain recipient, actor, or notification routing metadata.
        var data = Map.of("eventId", event.eventId(), "eventType", event.eventType(),
                "schemaVersion", event.schemaVersion(), "occurredAt", event.occurredAt(),
                "postId", event.postId(), "version", event.version(), "payload", event.payload());
        connections.values().stream().filter(c -> c.channels.contains("post-engagement"))
                .forEach(c -> send(c, SseEmitter.event().id(event.eventId().toString()).name(event.eventType()).data(data)));
        meters.timer("sse.post.delivery.delay").record(java.time.Duration.ofMillis(
                Math.max(0, System.currentTimeMillis() - event.occurredAt().toEpochMilli())));
    }

    @Scheduled(fixedDelay = 1000)
    public void notificationChanges() {
        if (!notificationsEnabled) return;
        Set<UUID> users = new HashSet<>();
        connections.values().stream().filter(c -> c.channels.contains("notifications")).forEach(c -> users.add(c.userId));
        for (var state : notifications.states(users)) {
            connections.values().stream().filter(c -> c.userId.equals(state.userId()) && c.channels.contains("notifications"))
                    .forEach(c -> {
                        long previous = c.version.getAndSet(state.version());
                        if (previous == state.version()) return;
                        UUID id = state.eventId() == null ? UUID.randomUUID() : state.eventId();
                        String type = state.eventType() == null ? "notification.unread-count.changed" : state.eventType();
                        var data = Map.of("eventId", id, "eventType", type, "schemaVersion", 1,
                                "occurredAt", Instant.now(), "version", state.version(),
                                "payload", Map.of("unreadCount", state.unreadCount()));
                        send(c, SseEmitter.event().id(id.toString()).name(type).data(data));
                        if (!type.equals("notification.unread-count.changed")) {
                            Map<String, Object> countData = new HashMap<>(data);
                            countData.put("eventType", "notification.unread-count.changed");
                            send(c, SseEmitter.event().id(id.toString()).name("notification.unread-count.changed").data(countData));
                        }
                    });
        }
    }

    @Scheduled(fixedDelay = 15000)
    public void heartbeat() {
        connections.values().forEach(c -> send(c, SseEmitter.event().comment("heartbeat")));
    }

    private void send(Connection connection, SseEmitter.SseEventBuilder event) {
        if (!connection.queue.offer(event)) { close(connection); return; }
        if (!connection.sending.compareAndSet(false, true)) return;
        try { executor.execute(() -> drain(connection)); }
        catch (RejectedExecutionException ex) { close(connection); }
    }

    private void drain(Connection connection) {
        try {
            SseEmitter.SseEventBuilder event;
            while (connections.containsKey(connection.emitter) && (event = connection.queue.poll()) != null)
                connection.emitter.send(event);
        } catch (IOException | RuntimeException ex) {
            meters.counter("sse.connections.errors").increment();
            close(connection);
        } finally {
            connection.sending.set(false);
            if (!connection.queue.isEmpty() && connections.containsKey(connection.emitter)
                    && connection.sending.compareAndSet(false, true)) {
                try { executor.execute(() -> drain(connection)); }
                catch (RejectedExecutionException ex) { close(connection); }
            }
        }
    }

    private void close(Connection connection) {
        connections.remove(connection.emitter);
        connection.queue.clear();
        connection.emitter.complete();
    }

    public void disconnect(UUID userId) {
        connections.values().stream().filter(connection -> connection.userId.equals(userId))
                .forEach(this::close);
    }

    @PreDestroy
    public void shutdown() { connections.values().forEach(this::close); }

    private static final class Connection {
        final UUID userId;
        final SseEmitter emitter;
        final Set<String> channels;
        final AtomicLong version = new AtomicLong(-1);
        final ArrayBlockingQueue<SseEmitter.SseEventBuilder> queue = new ArrayBlockingQueue<>(32);
        final java.util.concurrent.atomic.AtomicBoolean sending = new java.util.concurrent.atomic.AtomicBoolean();
        Connection(UUID userId, SseEmitter emitter, Set<String> channels) {
            this.userId = userId; this.emitter = emitter; this.channels = channels;
        }
    }
}
