package com.nongthinh.post_service.infra.persistence.adapter;

import com.nongthinh.post_service.application.event.PostEngagementEvent;
import com.nongthinh.post_service.application.port.out.PostEngagementRepository;
import com.nongthinh.post_service.application.view.PostEngagementView;
import com.nongthinh.post_service.domain.interaction.valueobject.ReactionType;
import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.EnumMap;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

@Repository
@RequiredArgsConstructor
public class PostEngagementRepositoryImpl implements PostEngagementRepository {
    private final JdbcTemplate jdbc;
    private final EntityManager entityManager;
    private final ObjectMapper json;

    @Override
    public void incrementVersion(UUID postId, boolean reaction) {
        // Flush JPA comment/deletion writes before the JDBC snapshot in the same transaction.
        entityManager.flush();
        jdbc.update(reaction
                ? "UPDATE posts SET reaction_version = reaction_version + 1 WHERE id = ?"
                : "UPDATE posts SET comment_version = comment_version + 1 WHERE id = ?", postId);
    }

    @Override
    public PostEngagementView snapshot(UUID postId, UUID actorId) {
        // One SQL statement gives counts and versions the same MVCC snapshot.
        return jdbc.queryForObject("""
                SELECT p.reaction_version, p.comment_version,
                  (SELECT reaction_type FROM post_reactions WHERE post_id=p.id AND actor_id=?) current_reaction,
                  (SELECT count(*) FROM post_comments c WHERE c.post_id=p.id
                    AND c.parent_comment_id IS NULL AND c.status='PUBLISHED' AND c.deleted_at IS NULL) roots,
                  (SELECT count(*) FROM post_comments c WHERE c.post_id=p.id
                    AND c.status='PUBLISHED' AND c.deleted_at IS NULL
                    AND (c.parent_comment_id IS NULL OR EXISTS (SELECT 1 FROM post_comments parent
                      WHERE parent.id=c.parent_comment_id AND parent.status='PUBLISHED'
                      AND parent.deleted_at IS NULL))) comments,
                  (SELECT coalesce(jsonb_object_agg(r.reaction_type, r.total), '{}'::jsonb)::text
                    FROM (SELECT reaction_type, count(*) total FROM post_reactions
                          WHERE post_id=p.id GROUP BY reaction_type) r) reactions
                FROM posts p WHERE p.id=?
                """, (rs, row) -> {
                    var counts = new EnumMap<ReactionType, Long>(ReactionType.class);
                    var node = json.readTree(rs.getString("reactions"));
                    for (var type : ReactionType.values()) counts.put(type, node.path(type.name()).asLong(0));
                    String current = rs.getString("current_reaction");
                    return new PostEngagementView(postId, counts,
                            counts.values().stream().mapToLong(Long::longValue).sum(),
                            rs.getLong("reaction_version"), current == null ? null : ReactionType.valueOf(current),
                            rs.getLong("roots"), rs.getLong("comments"), rs.getLong("comment_version"));
                }, actorId, postId);
    }

    @Override
    public void append(PostEngagementEvent event) {
        jdbc.update("""
                INSERT INTO post_outbox_events(id, aggregate_id, event_type, schema_version, payload, occurred_at)
                VALUES (?, ?, ?, ?, ?::jsonb, ?)
                """, event.eventId(), event.postId(), event.eventType(), event.schemaVersion(),
                json.writeValueAsString(event), Timestamp.from(event.occurredAt()));
    }
}
