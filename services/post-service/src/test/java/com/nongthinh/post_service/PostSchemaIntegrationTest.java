package com.nongthinh.post_service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nongthinh.post_service.application.port.out.repository.PostHistoryRepository;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.port.out.repository.CommentRepository;
import com.nongthinh.post_service.application.port.out.repository.ReactionRepository;
import com.nongthinh.post_service.domain.comment.Comment;
import com.nongthinh.post_service.domain.history.PostHistory;
import com.nongthinh.post_service.domain.history.PostSnapshot;
import com.nongthinh.post_service.domain.history.valueobject.HistoryActorType;
import com.nongthinh.post_service.domain.history.valueobject.PostHistoryAction;
import com.nongthinh.post_service.domain.interaction.Reaction;
import com.nongthinh.post_service.domain.interaction.valueobject.ReactionType;
import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.post.PostMedia;
import com.nongthinh.post_service.domain.post.valueobject.AuthorUserId;
import com.nongthinh.post_service.domain.post.valueobject.CropTypeId;
import com.nongthinh.post_service.domain.post.valueobject.FileId;
import com.nongthinh.post_service.domain.post.valueobject.MediaType;
import com.nongthinh.post_service.domain.post.valueobject.PostContent;
import com.nongthinh.post_service.domain.post.valueobject.PostId;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostTypeId;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "spring.profiles.active=dev",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/unused"
})
class PostSchemaIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("post_service")
            .withUsername("post_service")
            .withPassword("post_service");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired JdbcTemplate jdbc;
    @Autowired PostRepository posts;
    @Autowired PostHistoryRepository histories;
    @Autowired CommentRepository comments;
    @Autowired ReactionRepository reactions;
    @PersistenceContext EntityManager entityManager;

    @Test
    void migrationRunsAndSeedsCanonicalPostTypes() {
        Integer count = jdbc.queryForObject("SELECT count(*) FROM post_types", Integer.class);
        assertThat(count).isEqualTo(5);
        assertThat(jdbc.queryForList("SELECT code FROM post_types ORDER BY display_order", String.class))
                .containsExactly("QUESTION", "EXPERIENCE", "WARNING", "PRICE", "EVENT");
        assertThat(jdbc.queryForObject("""
                SELECT is_nullable FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND table_name = 'posts'
                  AND column_name = 'post_type_id'
                """, String.class)).isEqualTo("YES");
    }

    @Test
    @Transactional
    void publishedPostRequiresPublishedAt() {
        UUID typeId = jdbc.queryForObject("SELECT id FROM post_types WHERE code = 'QUESTION'", UUID.class);
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO posts (id, author_user_id, post_type_id, content, status, updated_at)
                VALUES (?, ?, ?, 'content', 'PUBLISHED', ?)
                """, UUID.randomUUID(), UUID.randomUUID(), typeId, Timestamp.from(Instant.now())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void reactionIsUniquePerActor() {
        UUID postId = insertPublishedPost();
        UUID actorId = UUID.randomUUID();
        Instant now = Instant.now();
        jdbc.update("INSERT INTO post_reactions (id, post_id, actor_id, reaction_type, updated_at) VALUES (?, ?, ?, 'LIKE', ?)",
                UUID.randomUUID(), postId, actorId, Timestamp.from(now));
        assertThatThrownBy(() -> jdbc.update("INSERT INTO post_reactions (id, post_id, actor_id, reaction_type, updated_at) VALUES (?, ?, ?, 'LOVE', ?)",
                UUID.randomUUID(), postId, actorId, Timestamp.from(now)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void reactionAdapterAtomicallyChangesExistingReaction() {
        UUID postId = insertPublishedPost();
        UUID actorId = UUID.randomUUID();
        UUID originalId = UUID.randomUUID();
        Instant now = Instant.now();

        Reaction first = reactions.upsert(Reaction.create(
                originalId, postId, actorId, ReactionType.LIKE, now));
        Reaction changed = reactions.upsert(Reaction.create(
                UUID.randomUUID(), postId, actorId, ReactionType.LOVE, now.plusSeconds(1)));

        assertThat(first.getId()).isEqualTo(originalId);
        assertThat(changed.getId()).isEqualTo(originalId);
        assertThat(changed.getType()).isEqualTo(ReactionType.LOVE);
        assertThat(reactions.countByPostId(postId))
                .containsEntry(ReactionType.LOVE, 1L)
                .doesNotContainKey(ReactionType.LIKE);
    }

    @Test
    @Transactional
    void commentAdapterPagesRootThreadsAndReplies() {
        UUID postId = insertPublishedPost();
        UUID rootAuthorId = UUID.randomUUID();
        Instant now = Instant.now();
        Comment root = comments.save(Comment.createRoot(
                UUID.randomUUID(), postId, rootAuthorId, "Root", now));
        Comment reply = comments.save(Comment.createReply(
                UUID.randomUUID(), root, UUID.randomUUID(), "Reply", now.plusSeconds(1)));

        var page = comments.findPublishedThreads(postId, 0, 20);

        assertThat(page.items()).singleElement().satisfies(thread -> {
            assertThat(thread.comment().getId()).isEqualTo(root.getId());
            assertThat(thread.replies()).extracting(Comment::getId)
                    .containsExactly(reply.getId());
        });
    }

    @Test
    @Transactional
    void postRepositoryPreservesExistingCropLinkCreationTime() {
        UUID typeId = jdbc.queryForObject(
                "SELECT id FROM post_types WHERE code = 'QUESTION'", UUID.class);
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID originalCropTypeId = UUID.randomUUID();
        UUID addedCropTypeId = UUID.randomUUID();
        Instant now = Instant.now();
        Post original = Post.createPublished(
                new PostId(postId), new AuthorUserId(authorId), new PostTypeId(typeId), null,
                new PostContent("content"), null, PostVisibility.PUBLIC, List.of(),
                Set.of(new CropTypeId(originalCropTypeId)), now, 10);
        posts.save(original);
        entityManager.flush();
        Instant originalLinkCreatedAt = jdbc.queryForObject("""
                SELECT created_at FROM post_crop_types
                WHERE post_id = ? AND crop_type_id = ?
                """, Timestamp.class, postId, originalCropTypeId).toInstant();

        Post loaded = posts.findById(new PostId(postId)).orElseThrow();
        loaded.edit(
                new AuthorUserId(authorId), new PostTypeId(typeId), null,
                new PostContent("changed"), null, PostVisibility.PUBLIC, List.of(),
                Set.of(new CropTypeId(originalCropTypeId), new CropTypeId(addedCropTypeId)),
                10, now.plusSeconds(1));
        posts.save(loaded);
        entityManager.flush();

        assertThat(jdbc.queryForObject("""
                SELECT created_at FROM post_crop_types
                WHERE post_id = ? AND crop_type_id = ?
                """, Timestamp.class, postId, originalCropTypeId).toInstant())
                .isEqualTo(originalLinkCreatedAt);
        assertThat(jdbc.queryForObject("""
                SELECT count(*) FROM post_crop_types WHERE post_id = ?
                """, Integer.class, postId)).isEqualTo(2);
    }

    @Test
    @Transactional
    void bookmarkIsUniquePerUser() {
        UUID postId = insertPublishedPost();
        UUID userId = UUID.randomUUID();
        jdbc.update("INSERT INTO post_bookmarks (post_id, user_id) VALUES (?, ?)", postId, userId);
        assertThatThrownBy(() -> jdbc.update(
                "INSERT INTO post_bookmarks (post_id, user_id) VALUES (?, ?)", postId, userId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void historyPreventsPhysicalPostDeletion() {
        UUID postId = insertPublishedPost();
        UUID authorId = jdbc.queryForObject("SELECT author_user_id FROM posts WHERE id = ?", UUID.class, postId);
        jdbc.update("""
                INSERT INTO post_histories (
                    id, post_id, post_author_user_id, actor_user_id, actor_type, action,
                    new_status, new_visibility, snapshot_data
                ) VALUES (?, ?, ?, ?, 'USER', 'CREATED', 'PUBLISHED', 'PUBLIC', CAST(? AS jsonb))
                """, UUID.randomUUID(), postId, authorId, authorId, "{\"content\":\"content\"}");

        assertThatThrownBy(() -> jdbc.update("DELETE FROM posts WHERE id = ?", postId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void jpaAdaptersPersistAggregateAndJsonSnapshot() {
        UUID typeId = jdbc.queryForObject("SELECT id FROM post_types WHERE code = 'QUESTION'", UUID.class);
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Instant now = Instant.now();
        Post post = Post.createPublished(new PostId(postId), new AuthorUserId(authorId),
                new PostTypeId(typeId), null, new PostContent("content"), null,
                PostVisibility.PUBLIC, List.of(PostMedia.create(UUID.randomUUID(),
                        new FileId(UUID.randomUUID()), MediaType.IMAGE,
                        "https://cdn.example.test/image.jpg", "image/jpeg", 800, 600,
                        10_000L, 0, null, now)), Set.of(new CropTypeId(UUID.randomUUID())),
                now, 10);
        posts.save(post);
        Post loaded = posts.findById(new PostId(postId)).orElseThrow();
        PostSnapshot snapshot = PostSnapshot.from(loaded);
        histories.append(PostHistory.create(UUID.randomUUID(), postId, authorId, authorId,
                HistoryActorType.USER, PostHistoryAction.CREATED, null, PostStatus.PUBLISHED,
                null, PostVisibility.PUBLIC, null, null, null, snapshot, now));
        entityManager.flush();

        assertThat(loaded.getMedia()).hasSize(1);
        assertThat(loaded.getCropTypeIds()).hasSize(1);
        assertThat(jdbc.queryForObject(
                "SELECT jsonb_typeof(snapshot_data) FROM post_histories WHERE post_id = ?",
                String.class, postId)).isEqualTo("object");
    }

    private UUID insertPublishedPost() {
        UUID postId = UUID.randomUUID();
        UUID typeId = jdbc.queryForObject("SELECT id FROM post_types WHERE code = 'QUESTION'", UUID.class);
        Instant now = Instant.now();
        jdbc.update("""
                INSERT INTO posts (
                    id, author_user_id, post_type_id, content, visibility, status,
                    published_at, updated_at
                ) VALUES (?, ?, ?, 'content', 'PUBLIC', 'PUBLISHED', ?, ?)
                """, postId, UUID.randomUUID(), typeId, Timestamp.from(now), Timestamp.from(now));
        return postId;
    }
}
