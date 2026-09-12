package com.nongthinh.notification_service.infra.persistence.notification;

import com.nongthinh.notification_service.application.event.PostEngagementEvent;
import com.nongthinh.notification_service.application.port.out.repository.NotificationRepository;
import com.nongthinh.notification_service.application.view.*;
import com.nongthinh.notification_service.common.exception.ErrorCode;
import com.nongthinh.notification_service.domain.exception.BusinessException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper json;
    private final io.micrometer.core.instrument.MeterRegistry meters;

    public boolean claimEvent(UUID id, Instant now) {
        boolean claimed = jdbc.update("INSERT INTO notification_processed_events VALUES (?, ?) ON CONFLICT DO NOTHING",
                id, Timestamp.from(now)) == 1;
        if (!claimed) meters.counter("notification.events.duplicate").increment();
        return claimed;
    }

    public boolean createOrCoalesce(PostEngagementEvent event, UUID id) {
        String conflict = "POST_REACTION".equals(event.notificationType()) ? """
                ON CONFLICT (actor_user_id, recipient_user_id, entity_id, type) WHERE type='POST_REACTION'
                DO UPDATE SET source_event_id=EXCLUDED.source_event_id, source_version=EXCLUDED.source_version,
                  payload=EXCLUDED.payload, created_at=EXCLUDED.created_at, read_at=NULL
                WHERE notifications.source_version < EXCLUDED.source_version
                """ : "ON CONFLICT (source_event_id) DO NOTHING";
        boolean changed = jdbc.update("""
                INSERT INTO notifications(id, recipient_user_id, actor_user_id, type, entity_type,
                  entity_id, source_event_id, source_version, payload, created_at)
                VALUES (?, ?, ?, ?, 'POST', ?, ?, ?, ?::jsonb, ?)
                """ + conflict, id, event.recipientUserId(), event.actorUserId(), event.notificationType(),
                event.postId(), event.eventId(), event.version(), json.writeValueAsString(event.payload()),
                Timestamp.from(event.occurredAt())) == 1;
        if (!changed) meters.counter("notification.events.stale").increment();
        return changed;
    }

    public NotificationPageView list(UUID userId, int page, int size) {
        var items = jdbc.query("""
                SELECT * FROM notifications WHERE recipient_user_id=?
                ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?
                """, (rs, row) -> new NotificationView(rs.getObject("id", UUID.class),
                rs.getObject("actor_user_id", UUID.class), rs.getString("type"), rs.getString("entity_type"),
                rs.getObject("entity_id", UUID.class), instant(rs, "read_at"), instant(rs, "created_at")),
                userId, size + 1, (long) page * size);
        return new NotificationPageView(List.copyOf(items.subList(0, Math.min(size, items.size()))),
                page, size, items.size() > size);
    }

    public NotificationStateView state(UUID userId) {
        return states(List.of(userId)).getFirst();
    }

    public List<NotificationStateView> states(Collection<UUID> users) {
        if (users.isEmpty()) return List.of();
        String values = String.join(",", Collections.nCopies(users.size(), "(?::uuid)"));
        return jdbc.query("""
                SELECT u.id, coalesce(s.version, 0) version, s.event_id, s.event_type,
                  (SELECT count(*) FROM notifications n WHERE n.recipient_user_id=u.id AND n.read_at IS NULL) unread
                FROM (VALUES %s) u(id) LEFT JOIN notification_user_state s ON s.user_id=u.id
                """.formatted(values), (rs, row) -> new NotificationStateView(rs.getObject("id", UUID.class),
                rs.getLong("version"), rs.getLong("unread"), rs.getObject("event_id", UUID.class),
                rs.getString("event_type")), users.toArray());
    }

    public boolean markRead(UUID userId, UUID id, Instant now) {
        var rows = jdbc.queryForList("SELECT read_at FROM notifications WHERE id=? AND recipient_user_id=? FOR UPDATE",
                id, userId);
        if (rows.isEmpty()) throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
        if (rows.getFirst().get("read_at") != null) return false;
        return jdbc.update("UPDATE notifications SET read_at=? WHERE id=? AND recipient_user_id=? AND read_at IS NULL",
                Timestamp.from(now), id, userId) > 0;
    }

    public boolean markAllRead(UUID userId, Instant now) {
        return jdbc.update("UPDATE notifications SET read_at=? WHERE recipient_user_id=? AND read_at IS NULL",
                Timestamp.from(now), userId) > 0;
    }

    public void changed(UUID userId, UUID eventId, String type) {
        jdbc.update("""
                INSERT INTO notification_user_state(user_id, version, event_id, event_type) VALUES (?, 1, ?, ?)
                ON CONFLICT(user_id) DO UPDATE SET version=notification_user_state.version+1,
                  event_id=EXCLUDED.event_id, event_type=EXCLUDED.event_type
                """, userId, eventId, type);
    }

    private Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }
}
