package com.nongthinh.post_service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.nongthinh.post_service.application.command.*;
import com.nongthinh.post_service.application.port.in.postcomment.*;
import com.nongthinh.post_service.application.port.in.postreaction.*;
import com.nongthinh.post_service.application.port.out.PostEngagementRepository;
import com.nongthinh.post_service.infra.messaging.PostOutboxPublisher;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.support.TransactionTemplate;
import com.nongthinh.post_service.domain.interaction.valueobject.ReactionType;

@EnabledIfSystemProperty(named = "sse.local.integration", matches = "true")
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://127.0.0.1:55433/sse_post_test",
        "spring.datasource.username=postgres", "spring.datasource.password=unused",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://127.0.0.1/unused",
        "post-outbox.enabled=false"
})
class PostEngagementPersistenceTest {
    private static final UUID AUTHOR = UUID.fromString("80000000-0000-0000-0000-000000000001");
    private static final UUID ACTOR = UUID.fromString("80000000-0000-0000-0000-000000000002");
    @Autowired JdbcTemplate jdbc;
    @Autowired SetPostReactionUseCase setReaction;
    @Autowired RemovePostReactionUseCase removeReaction;
    @Autowired CreatePostCommentUseCase createComment;
    @Autowired DeletePostCommentUseCase deleteComment;
    @Autowired PostEngagementRepository metrics;
    @Autowired TransactionTemplate transactions;
    private UUID postId;

    @BeforeEach
    void seed() {
        postId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO posts(id, author_user_id, content, visibility, status, published_at, created_at, updated_at)
                VALUES (?, ?, 'SSE test', 'PUBLIC', 'PUBLISHED', NOW(), NOW(), NOW())
                """, postId, AUTHOR);
        authenticate(ACTOR);
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        jdbc.update("DELETE FROM post_outbox_events WHERE aggregate_id=?", postId);
        jdbc.update("DELETE FROM post_comments WHERE post_id=? AND parent_comment_id IS NOT NULL", postId);
        jdbc.update("DELETE FROM post_comments WHERE post_id=?", postId);
        jdbc.update("DELETE FROM post_reactions WHERE post_id=?", postId);
        jdbc.update("DELETE FROM posts WHERE id=?", postId);
    }

    @Test
    void countsVersionsOutboxAndReplySemantics() {
        var first = setReaction.execute(postId, new SetPostReactionCommand(ReactionType.LIKE));
        var changed = setReaction.execute(postId, new SetPostReactionCommand(ReactionType.LOVE));
        var removed = removeReaction.execute(postId);
        assertThat(first.reactionVersion()).isEqualTo(1);
        assertThat(changed.reactionVersion()).isEqualTo(2);
        assertThat(changed.reactionTotal()).isEqualTo(1);
        assertThat(changed.reactionCounts().get(ReactionType.LIKE)).isZero();
        assertThat(removed.reactionVersion()).isEqualTo(3);
        assertThat(removed.reactionTotal()).isZero();
        var root = createComment.execute(postId, new CreatePostCommentCommand("Root", null));
        authenticate(AUTHOR);
        var reply = createComment.execute(postId, new CreatePostCommentCommand("Reply", root.id()));
        assertThat(reply.commentRootTotal()).isEqualTo(1);
        assertThat(reply.commentTotal()).isEqualTo(2);
        assertThat(reply.commentVersion()).isEqualTo(2);
        authenticate(ACTOR);
        var deleted = deleteComment.execute(postId, root.id());
        assertThat(deleted.commentRootTotal()).isZero();
        assertThat(deleted.commentTotal()).isZero();
        assertThat(metrics.snapshot(postId, ACTOR).reactionVersion()).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM post_outbox_events WHERE aggregate_id=?", Long.class, postId)).isEqualTo(6);
    }

    @Test
    void rollbackLeavesNeitherReactionNorOutbox() {
        assertThatThrownBy(() -> transactions.execute(status -> {
            setReaction.execute(postId, new SetPostReactionCommand(ReactionType.LIKE));
            throw new IllegalStateException("Force rollback");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(metrics.snapshot(postId, ACTOR).reactionVersion()).isZero();
        assertThat(metrics.snapshot(postId, ACTOR).reactionTotal()).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM post_outbox_events WHERE aggregate_id=?", Long.class, postId)).isZero();
    }

    @Test
    void concurrentActorsReceiveDistinctVersionsAndExactCounts() throws Exception {
        try (var pool = Executors.newFixedThreadPool(2)) {
            CountDownLatch start = new CountDownLatch(1);
            var a = pool.submit(() -> reactTogether(ACTOR, start));
            var b = pool.submit(() -> reactTogether(AUTHOR, start));
            start.countDown();
            var first = a.get(10, TimeUnit.SECONDS);
            var second = b.get(10, TimeUnit.SECONDS);
            assertThat(List.of(first.reactionVersion(), second.reactionVersion())).containsExactlyInAnyOrder(1L, 2L);
            assertThat(first.reactionTotal()).isEqualTo(first.reactionVersion());
            assertThat(second.reactionTotal()).isEqualTo(second.reactionVersion());
            assertThat(metrics.snapshot(postId, ACTOR).reactionTotal()).isEqualTo(2);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void publisherRetriesSameEventAndMarksPublishedOnlyAfterAcknowledgment() {
        setReaction.execute(postId, new SetPostReactionCommand(ReactionType.LIKE));
        KafkaTemplate<String, String> kafka = mock(KafkaTemplate.class);
        when(kafka.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("Broker unavailable")))
                .thenReturn(CompletableFuture.completedFuture(null));
        var publisher = new PostOutboxPublisher(jdbc, kafka, transactions, new SimpleMeterRegistry(),
                new com.nongthinh.post_service.configuration.PostOutboxProperties(true, "post.engagement.v1", 1));
        publisher.publish();
        var failed = jdbc.queryForMap("SELECT published_at, attempt_count FROM post_outbox_events WHERE aggregate_id=?", postId);
        assertThat(failed.get("published_at")).isNull();
        assertThat(failed.get("attempt_count")).isEqualTo(1);
        jdbc.update("UPDATE post_outbox_events SET next_attempt_at=NOW() WHERE aggregate_id=?", postId);
        publisher.publish();
        assertThat(jdbc.queryForObject("SELECT published_at IS NOT NULL FROM post_outbox_events WHERE aggregate_id=?", Boolean.class, postId)).isTrue();
        var payload = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(kafka, times(2)).send(eq("post.engagement.v1"), eq(postId.toString()), payload.capture());
        assertThat(payload.getAllValues().get(0)).isEqualTo(payload.getAllValues().get(1));
    }

    private com.nongthinh.post_service.application.view.PostReactionView reactTogether(UUID userId, CountDownLatch start) throws Exception {
        authenticate(userId);
        try { start.await(); return setReaction.execute(postId, new SetPostReactionCommand(ReactionType.LIKE)); }
        finally { SecurityContextHolder.clearContext(); }
    }

    private void authenticate(UUID id) {
        var jwt = Jwt.withTokenValue("test").header("alg", "none").claim("nongthinh_id", id.toString())
                .issuedAt(Instant.parse("2026-09-09T00:00:00Z")).expiresAt(Instant.parse("2030-01-01T00:00:00Z")).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));
    }
}
