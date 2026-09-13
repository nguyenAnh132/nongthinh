package com.nongthinh.profile_service.infra.messaging;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
@Component
@Slf4j
@ConditionalOnProperty(name="profile-follow.outbox-enabled", havingValue="true", matchIfMissing=true)
public class FollowNotificationOutboxPublisher {
    private final JdbcTemplate jdbc;
    private final KafkaTemplate<String, String> kafka;
    private final TransactionTemplate transactions;
    private final String topic;
    public FollowNotificationOutboxPublisher(JdbcTemplate jdbc, KafkaTemplate<String, String> kafka,
            TransactionTemplate transactions, @Value("${PROFILE_NOTIFICATION_TOPIC:profile.notifications.v1}") String topic) {
        this.jdbc = jdbc;
        this.kafka = kafka;
        this.transactions = transactions;
        this.topic = topic;
    }
    @Scheduled(fixedDelayString="${profile-follow.poll-ms:500}")
    public void publish() {
        for (int i = 0; i < 50; i++) {
            Boolean found = transactions.execute(status -> {
                var rows = jdbc.queryForList("""
                        SELECT id, recipient_user_id, jsonb_build_object(
                          'eventId', id, 'schemaVersion', 1, 'occurredAt', occurred_at,
                          'actorUserId', actor_user_id, 'recipientUserId', recipient_user_id,
                          'notificationType', notification_type, 'entityId', entity_id)::text payload
                        FROM profile_notification_outbox
                        WHERE published_at IS NULL AND next_attempt_at <= NOW()
                        ORDER BY occurred_at, id LIMIT 1 FOR UPDATE SKIP LOCKED
                        """);
                if (rows.isEmpty()) return false;
                var row = rows.getFirst();
                try {
                    kafka.send(topic, row.get("recipient_user_id").toString(), row.get("payload").toString())
                            .get(10, TimeUnit.SECONDS);
                    jdbc.update("UPDATE profile_notification_outbox SET published_at=NOW() WHERE id=?", row.get("id"));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Follow notification publisher interrupted", ex);
                } catch (Exception ex) {
                    jdbc.update("""
                            UPDATE profile_notification_outbox SET attempt_count=attempt_count+1,
                              next_attempt_at=NOW()+LEAST(60, power(2, LEAST(attempt_count, 6)))*interval '1 second'
                            WHERE id=?
                            """, row.get("id"));
                    log.warn("Follow notification publish failed for event {}", row.get("id"), ex);
                }
                return true;
            });
            if (!Boolean.TRUE.equals(found) || Thread.currentThread().isInterrupted()) break;
        }
    }
}
