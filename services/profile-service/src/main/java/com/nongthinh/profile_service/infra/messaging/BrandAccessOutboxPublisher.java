package com.nongthinh.profile_service.infra.messaging;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
@ConditionalOnProperty(name = "brand-access.outbox-enabled", havingValue = "true", matchIfMissing = true)
public class BrandAccessOutboxPublisher {
    private final JdbcTemplate jdbc;
    private final KafkaTemplate<String, String> kafka;
    private final TransactionTemplate transactions;
    private final String topic;

    public BrandAccessOutboxPublisher(JdbcTemplate jdbc, KafkaTemplate<String, String> kafka,
            TransactionTemplate transactions, @Value("${messaging.kafka.topics.brand-access-changed}") String topic) {
        this.jdbc = jdbc;
        this.kafka = kafka;
        this.transactions = transactions;
        this.topic = topic;
    }

    @Scheduled(fixedDelayString = "${brand-access.outbox-poll-ms:1000}")
    public void publish() {
        for (int i = 0; i < 50; i++) {
            Boolean found = transactions.execute(transaction -> {
                var rows = jdbc.queryForList("""
                        SELECT id, user_id, jsonb_build_object(
                            'eventId', id, 'userId', user_id, 'status', status, 'occurredAt', occurred_at)::text payload
                        FROM brand_access_outbox
                        WHERE published_at IS NULL AND next_attempt_at <= now()
                        ORDER BY occurred_at, id LIMIT 1 FOR UPDATE SKIP LOCKED
                        """);
                if (rows.isEmpty()) return false;
                var row = rows.getFirst();
                try {
                    kafka.send(topic, row.get("user_id").toString(), row.get("payload").toString())
                            .get(10, TimeUnit.SECONDS);
                    jdbc.update("UPDATE brand_access_outbox SET published_at=now() WHERE id=?", row.get("id"));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Brand access publisher interrupted", ex);
                } catch (ExecutionException | TimeoutException | org.springframework.kafka.KafkaException ex) {
                    jdbc.update("""
                            UPDATE brand_access_outbox SET attempt_count=attempt_count+1,
                                next_attempt_at=now()+interval '10 seconds' WHERE id=?
                            """, row.get("id"));
                    log.warn("Brand access event publish failed | eventId={}", row.get("id"), ex);
                }
                return true;
            });
            if (!Boolean.TRUE.equals(found) || Thread.currentThread().isInterrupted()) break;
        }
    }
}
