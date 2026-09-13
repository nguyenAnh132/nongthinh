package com.nongthinh.profile_service;

import com.nongthinh.profile_service.domain.follow.UserFollow;
import com.nongthinh.profile_service.infra.persistence.follow.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static org.assertj.core.api.Assertions.*;

@EnabledIfSystemProperty(named="follow.local.integration", matches="true")
class LocalFollowPersistenceTest {
    private final Instant now = Instant.parse("2026-09-12T00:00:00Z");
    private UUID id(int value) { return UUID.fromString("00000000-0000-0000-0000-%012d".formatted(value)); }

    @Test void migratesAndPersistsPagedFollowsWithTransactionalNotifications() {
        // Explicitly local, dedicated test server: never use the application's datasource configuration.
        var data = new DriverManagerDataSource("jdbc:postgresql://127.0.0.1:55439/postgres", "postgres", "");
        String schema = "follow_test_" + System.nanoTime();
        Flyway.configure().dataSource(data).schemas(schema).defaultSchema(schema).load().migrate();
        var scoped = new DriverManagerDataSource(
                "jdbc:postgresql://127.0.0.1:55439/postgres?currentSchema=" + schema, "postgres", "");
        var jdbc = new JdbcTemplate(scoped);
        var tx = new TransactionTemplate(new DataSourceTransactionManager(scoped));
        var follows = new UserFollowRepositoryImpl(jdbc);
        var notifications = new FollowNotificationRepositoryImpl(jdbc);
        for (int i = 1; i <= 24; i++) {
            jdbc.update("""
                    INSERT INTO farmer_profiles(id,user_id,first_name,last_name,gender,phone,status,created_at,updated_at)
                    VALUES (?,?,'Farmer',?,'MALE','0900000000','ACTIVE',?,?)
                    """, id(i), id(i), Integer.toString(i), Timestamp.from(now), Timestamp.from(now));
        }
        jdbc.update("""
                INSERT INTO brand_profiles(id,user_id,brand_name,phone,representative_name,representative_phone,
                    representative_email,status,created_at,updated_at)
                VALUES (?,?,'Brand','0900000000','Representative','0900000000','test@example.invalid','ACTIVE',?,?)
                """, id(100), id(100), Timestamp.from(now), Timestamp.from(now));
        for (int i = 1; i <= 22; i++) {
            assertThat(follows.add(UserFollow.create(id(200 + i), id(i), id(100), now))).isTrue();
        }
        assertThat(follows.add(UserFollow.create(id(300), id(1), id(100), now))).isFalse();
        assertThat(follows.profile(id(100), id(1)).orElseThrow().followerCount()).isEqualTo(22);
        assertThat(follows.profile(id(1), id(100)).orElseThrow().followingCount()).isEqualTo(1);
        var first = follows.list(id(100), id(1), true, 0, 20);
        var second = follows.list(id(100), id(1), true, 1, 20);
        assertThat(first.items()).hasSize(20);
        assertThat(first.hasNext()).isTrue();
        assertThat(second.items()).hasSize(2);
        assertThat(second.hasNext()).isFalse();
        assertThat(first.items()).doesNotContainAnyElementsOf(second.items());
        assertThat(follows.following(id(1), List.of(id(100), id(2)))).containsExactly(id(100));

        // Two concurrent identical requests must commit just one relation and one notification.
        Runnable add = () -> tx.executeWithoutResult(status -> {
            var relation = UserFollow.create(UUID.randomUUID(), id(23), id(100), now);
            if (follows.add(relation)) notifications.newFollower(relation.getId(), id(23), id(100), now);
        });
        CompletableFuture.allOf(CompletableFuture.runAsync(add), CompletableFuture.runAsync(add)).join();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM profile_notification_outbox WHERE notification_type='NEW_FOLLOWER'", Long.class)).isEqualTo(1);

        // A late follower is excluded, and redelivery of the publication cannot fan out twice.
        follows.add(UserFollow.create(id(400), id(24), id(100), now.plusSeconds(10)));
        tx.executeWithoutResult(status -> notifications.publishedPost(id(500), id(100), now.plusSeconds(5)));
        tx.executeWithoutResult(status -> notifications.publishedPost(id(500), id(100), now.plusSeconds(5)));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM profile_notification_outbox WHERE notification_type='FOLLOWED_USER_POST'", Long.class)).isEqualTo(23);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM profile_notification_outbox WHERE recipient_user_id=?", Long.class, id(24))).isZero();

        tx.executeWithoutResult(status -> {
            var relation = UserFollow.create(id(600), id(100), id(1), now);
            follows.add(relation);
            notifications.newFollower(relation.getId(), id(100), id(1), now);
            status.setRollbackOnly();
        });
        assertThat(follows.following(id(100), List.of(id(1)))).isEmpty();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM profile_notification_outbox WHERE id=?", Long.class, id(600))).isZero();
        follows.remove(id(1), id(100));
        follows.remove(id(1), id(100));
        assertThat(follows.following(id(1), List.of(id(100)))).isEmpty();
        jdbc.update("UPDATE farmer_profiles SET status='DISABLED' WHERE user_id=?", id(2));
        assertThat(follows.profile(id(100), id(1)).orElseThrow().followerCount()).isEqualTo(22);
    }
}
