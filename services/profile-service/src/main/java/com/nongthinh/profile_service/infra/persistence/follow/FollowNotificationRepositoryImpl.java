package com.nongthinh.profile_service.infra.persistence.follow;
import com.nongthinh.profile_service.application.port.out.FollowNotificationRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
@RequiredArgsConstructor
public class FollowNotificationRepositoryImpl implements FollowNotificationRepository {
    private final JdbcTemplate jdbc;
    public void newFollower(UUID eventId, UUID actor, UUID recipient, Instant now) {
        jdbc.update("""
                INSERT INTO profile_notification_outbox(id, actor_user_id, recipient_user_id, notification_type, entity_id, occurred_at)
                VALUES (?, ?, ?, 'NEW_FOLLOWER', ?, ?)
                """, eventId, actor, recipient, actor, Timestamp.from(now));
    }
    public void publishedPost(UUID postId, UUID author, Instant occurredAt) {
        if (jdbc.update("INSERT INTO profile_processed_posts VALUES (?, NOW()) ON CONFLICT DO NOTHING", postId) == 0) return;
        // Set-based fan-out avoids loading an author's complete follower list into application memory.
        // Followers added after publication do not receive notifications about old posts.
        jdbc.update("""
                INSERT INTO profile_notification_outbox(id, actor_user_id, recipient_user_id, notification_type, entity_id, occurred_at)
                SELECT gen_random_uuid(), ?, f.follower_user_id, 'FOLLOWED_USER_POST', ?, ?
                FROM user_follows f WHERE f.followed_user_id=? AND f.created_at <= ?
                  AND (EXISTS (SELECT 1 FROM farmer_profiles p WHERE p.user_id=f.follower_user_id AND p.status='ACTIVE')
                    OR EXISTS (SELECT 1 FROM brand_profiles p WHERE p.user_id=f.follower_user_id AND p.status='ACTIVE'))
                """, author, postId, Timestamp.from(occurredAt), author, Timestamp.from(occurredAt));
    }
}
