package com.nongthinh.post_service.infra.messaging;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.nongthinh.post_service.configuration.PostOutboxProperties;
import com.nongthinh.post_service.configuration.KafkaTopicProperties;
import java.util.concurrent.atomic.AtomicLong;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import io.micrometer.core.instrument.MeterRegistry;

@Component
@ConditionalOnProperty(name = "post-outbox.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class PostOutboxPublisher {
    private final JdbcTemplate jdbc;
    private final KafkaTemplate<String, String> kafka;
    private final TransactionTemplate transactions;
    private final MeterRegistry meters;
    private final PostOutboxProperties properties;
    private final KafkaTopicProperties topics;
    private final AtomicLong pending = new AtomicLong();
    @PostConstruct
    void registerMetrics() { meters.gauge("post.outbox.pending", pending); }

    @Scheduled(fixedDelayString = "${post-outbox.poll-ms:500}")
    public void publish() {
        // Lock one row per short transaction; SKIP LOCKED allows multiple publishers.
        for (int i = 0; i < properties.batchSize(); i++) {
            Boolean found = transactions.execute(status -> {
                var rows = jdbc.queryForList("""
                        SELECT e.id, e.aggregate_id, e.payload::text FROM post_outbox_events e
                        WHERE e.published_at IS NULL AND e.next_attempt_at <= NOW()
                        AND NOT EXISTS (SELECT 1 FROM post_outbox_events earlier
                          WHERE earlier.aggregate_id=e.aggregate_id AND earlier.published_at IS NULL
                          AND earlier.sequence_no < e.sequence_no)
                        ORDER BY e.sequence_no LIMIT 1 FOR UPDATE OF e SKIP LOCKED
                        """);
                if (rows.isEmpty()) return false;
                var row = rows.getFirst();
                UUID id = (UUID) row.get("id");
                try {
                    kafka.send(topics.postEngagement(), row.get("aggregate_id").toString(), row.get("payload").toString())
                            .get(10, TimeUnit.SECONDS);
                    jdbc.update("UPDATE post_outbox_events SET published_at=NOW(), attempt_count=attempt_count+1 WHERE id=?", id);
                    meters.counter("post.outbox.published").increment();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Outbox publisher interrupted", ex);
                } catch (Exception ex) {
                    jdbc.update("""
                            UPDATE post_outbox_events SET attempt_count=attempt_count+1,
                              next_attempt_at=NOW() + LEAST(60, power(2, LEAST(attempt_count, 6))) * interval '1 second'
                            WHERE id=?
                            """, id);
                    meters.counter("post.outbox.failures").increment();
                    log.warn("Post outbox publish failed for event {}", id, ex);
                }
                return true;
            });
            if (!Boolean.TRUE.equals(found) || Thread.currentThread().isInterrupted()) break;
        }
    }

    @Scheduled(fixedDelay = 30000)
    public void pendingMetric() {
        pending.set(jdbc.queryForObject(
                "SELECT count(*) FROM post_outbox_events WHERE published_at IS NULL", Long.class));
    }
}
