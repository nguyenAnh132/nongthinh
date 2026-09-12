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
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@EnabledIfSystemProperty(named = "post.local.integration", matches = "true")
@SpringBootTest(properties = {
        "spring.profiles.active=dev",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost/unused"
})
class LocalPostSchemaVerificationTest {
    @Autowired JdbcTemplate jdbc;
    @Autowired PostRepository posts;
    @Autowired PostHistoryRepository histories;
    @Autowired CommentRepository comments;
    @Autowired ReactionRepository reactions;
    @PersistenceContext EntityManager entityManager;

    @Test
    void flywayAndJpaValidationLoadTheCompleteSchema() {
        assertThat(jdbc.queryForObject("SELECT count(*) FROM post_types", Integer.class)).isEqualTo(5);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'post_histories'", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT data_type FROM information_schema.columns WHERE table_name = 'post_media' AND column_name = 'media_url'", String.class)).isEqualTo("character varying");
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
    void databaseEnforcesReactionUniqueness() {
        UUID postId = insertPublishedPost();
        UUID actorId = UUID.randomUUID();
        Instant now = Instant.now();
        jdbc.update("INSERT INTO post_reactions (id, post_id, actor_id, reaction_type, updated_at) VALUES (?, ?, ?, 'LIKE', ?)", UUID.randomUUID(), postId, actorId, Timestamp.from(now));
        assertThatThrownBy(() -> jdbc.update("INSERT INTO post_reactions (id, post_id, actor_id, reaction_type, updated_at) VALUES (?, ?, ?, 'LOVE', ?)", UUID.randomUUID(), postId, actorId, Timestamp.from(now))).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void interactionAdaptersPersistThreadsAndAtomicallyReplaceReaction() {
        UUID postId = insertPublishedPost();
        UUID actorId = UUID.randomUUID();
        UUID originalReactionId = UUID.randomUUID();
        Instant now = Instant.now();
        Reaction first = reactions.upsert(Reaction.create(
                originalReactionId, postId, actorId, ReactionType.LIKE, now));
        Reaction changed = reactions.upsert(Reaction.create(
                UUID.randomUUID(), postId, actorId, ReactionType.LOVE, now.plusSeconds(1)));

        Comment root = comments.save(Comment.createRoot(
                UUID.randomUUID(), postId, actorId, "Root", now));
        Comment reply = comments.save(Comment.createReply(
                UUID.randomUUID(), root, UUID.randomUUID(), "Reply", now.plusSeconds(1)));
        var commentPage = comments.findPublishedThreads(postId, 0, 20);

        assertThat(first.getId()).isEqualTo(originalReactionId);
        assertThat(changed.getId()).isEqualTo(originalReactionId);
        assertThat(changed.getType()).isEqualTo(ReactionType.LOVE);
        assertThat(reactions.countByPostId(postId))
                .containsEntry(ReactionType.LOVE, 1L)
                .doesNotContainKey(ReactionType.LIKE);
        assertThat(commentPage.items()).singleElement().satisfies(thread -> {
            assertThat(thread.comment().getId()).isEqualTo(root.getId());
            assertThat(thread.replies()).extracting(Comment::getId)
                    .containsExactly(reply.getId());
        });
    }

    @Test
    @Transactional
    void repositorySynchronizesCropLinksWithoutRecreatingExistingRows() {
        UUID typeId = jdbc.queryForObject(
                "SELECT id FROM post_types WHERE code = 'QUESTION'", UUID.class);
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID originalCropTypeId = UUID.randomUUID();
        UUID addedCropTypeId = UUID.randomUUID();
        Instant now = Instant.now();
        posts.save(Post.createPublished(
                new PostId(postId), new AuthorUserId(authorId), new PostTypeId(typeId), null,
                new PostContent("content"), null, PostVisibility.PUBLIC, List.of(),
                Set.of(new CropTypeId(originalCropTypeId)), now, 10));
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
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM post_crop_types WHERE post_id = ?",
                Integer.class, postId)).isEqualTo(2);
    }

    @Test
    @Transactional
    void databaseEnforcesBookmarkUniqueness() {
        UUID postId = insertPublishedPost();
        UUID userId = UUID.randomUUID();
        jdbc.update("INSERT INTO post_bookmarks (post_id, user_id) VALUES (?, ?)", postId, userId);
        assertThatThrownBy(() -> jdbc.update("INSERT INTO post_bookmarks (post_id, user_id) VALUES (?, ?)", postId, userId)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void immutableHistoryRestrictsPhysicalPostDeletion() {
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

    @Test
    @Transactional
    void repositoryPagesPublicAndAuthorPostsWithAggregateChildren() {
        UUID typeId = jdbc.queryForObject(
                "SELECT id FROM post_types WHERE code = 'QUESTION'", UUID.class);
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID cropTypeId = UUID.randomUUID();
        Instant now = Instant.now();
        posts.save(Post.createPublished(
                new PostId(postId), new AuthorUserId(authorId), new PostTypeId(typeId), null,
                new PostContent("public page query"), null, PostVisibility.PUBLIC,
                List.of(PostMedia.create(
                        UUID.randomUUID(), new FileId(UUID.randomUUID()), MediaType.IMAGE,
                        "https://cdn.example.test/page.jpg", "image/jpeg", null, null,
                        1_000L, 0, null, now)),
                Set.of(new CropTypeId(cropTypeId)), now, 10
        ));

        var publicPage = posts.findPublic(typeId, null, cropTypeId, "page query", 0, 20);
        var publicPageWithoutKeyword = posts.findPublic(
                typeId, null, cropTypeId, null, 0, 20);
        var authorPage = posts.findByAuthor(authorId, PostStatus.PUBLISHED, 0, 20);

        assertThat(publicPage.items()).singleElement().satisfies(post -> {
            assertThat(post.getId().value()).isEqualTo(postId);
            assertThat(post.getMedia()).hasSize(1);
            assertThat(post.getCropTypeIds()).containsExactly(new CropTypeId(cropTypeId));
        });
        assertThat(publicPageWithoutKeyword.items())
                .extracting(post -> post.getId().value())
                .containsExactly(postId);
        assertThat(authorPage.items()).extracting(post -> post.getId().value())
                .containsExactly(postId);
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
