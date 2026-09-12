package com.nongthinh.post_service.infra.persistence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.nongthinh.post_service.configuration.PostLimitsProperties;
import com.nongthinh.post_service.domain.comment.Comment;
import com.nongthinh.post_service.domain.history.PostHistory;
import com.nongthinh.post_service.domain.history.PostSnapshot;
import com.nongthinh.post_service.domain.history.valueobject.HistoryActorType;
import com.nongthinh.post_service.domain.history.valueobject.PostHistoryAction;
import com.nongthinh.post_service.domain.interaction.Bookmark;
import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.post.PostMedia;
import com.nongthinh.post_service.domain.post.PostType;
import com.nongthinh.post_service.domain.post.valueobject.AuthorUserId;
import com.nongthinh.post_service.domain.post.valueobject.CropTypeId;
import com.nongthinh.post_service.domain.post.valueobject.FileId;
import com.nongthinh.post_service.domain.post.valueobject.MediaType;
import com.nongthinh.post_service.domain.post.valueobject.PostContent;
import com.nongthinh.post_service.domain.post.valueobject.PostId;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostTypeId;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostBookmarkEntity;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostCommentEntity;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostEntity;
import com.nongthinh.post_service.infra.persistence.entity.JpaPostHistoryEntity;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class PersistenceMapperTest {
    private static final Instant NOW = Instant.parse("2026-08-21T00:00:00Z");

    private final CatalogPersistenceMapper catalogMapper =
            Mappers.getMapper(CatalogPersistenceMapper.class);
    private final InteractionPersistenceMapper interactionMapper =
            Mappers.getMapper(InteractionPersistenceMapper.class);
    private final PostHistoryPersistenceMapper historyMapper =
            Mappers.getMapper(PostHistoryPersistenceMapper.class);
    private final PostPersistenceMapper postMapper =
            Mappers.getMapper(PostPersistenceMapper.class);

    @Test
    void mapsCatalogWithGeneratedMapperAndReconstructsDomain() {
        PostType domain = PostType.create(UUID.randomUUID(), "QUESTION", "Question",
                "Technical question", 10, NOW);

        var entity = catalogMapper.toEntity(domain);
        PostType reconstructed = catalogMapper.toDomain(entity);

        assertThat(entity.getCode()).isEqualTo("QUESTION");
        assertThat(reconstructed.getId()).isEqualTo(domain.getId());
        assertThat(reconstructed.getDescription()).isEqualTo("Technical question");
    }

    @Test
    void mapsInteractionEnumsAndEmbeddedBookmarkId() {
        Comment comment = Comment.createRoot(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), "Content", NOW);
        Bookmark bookmark = new Bookmark(UUID.randomUUID(), UUID.randomUUID(), NOW);

        JpaPostCommentEntity commentEntity = interactionMapper.toEntity(comment);
        JpaPostBookmarkEntity bookmarkEntity = interactionMapper.toEntity(bookmark);

        assertThat(commentEntity.getStatus()).isEqualTo("PUBLISHED");
        assertThat(interactionMapper.toDomain(commentEntity).getContent()).isEqualTo("Content");
        assertThat(bookmarkEntity.getId().getPostId()).isEqualTo(bookmark.postId());
        assertThat(interactionMapper.toDomain(bookmarkEntity)).isEqualTo(bookmark);
    }

    @Test
    void mapsHistoryEnumsAndSnapshot() {
        PostSnapshot snapshot = new PostSnapshot("Content", UUID.randomUUID(), null,
                Set.of(), null, List.of(), PostStatus.PUBLISHED, PostVisibility.PUBLIC);
        PostHistory history = PostHistory.create(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(), HistoryActorType.USER,
                PostHistoryAction.CREATED, null, PostStatus.PUBLISHED, null,
                PostVisibility.PUBLIC, null, null, null, snapshot, NOW);

        JpaPostHistoryEntity entity = historyMapper.toEntity(history);
        PostHistory reconstructed = historyMapper.toDomain(entity);

        assertThat(entity.getActorType()).isEqualTo("USER");
        assertThat(entity.getAction()).isEqualTo("CREATED");
        assertThat(reconstructed.getSnapshot()).isEqualTo(snapshot);
    }

    @Test
    void mapsCompletePostAggregateRoundTrip() {
        CropTypeId cropTypeId = new CropTypeId(UUID.randomUUID());
        PostMedia media = PostMedia.create(UUID.randomUUID(), new FileId(UUID.randomUUID()),
                MediaType.IMAGE, "https://cdn.example.test/image.jpg", "image/jpeg",
                800, 600, 10_000L, 0, "Rice", NOW);
        Post post = Post.createPublished(new PostId(UUID.randomUUID()),
                new AuthorUserId(UUID.randomUUID()), new PostTypeId(UUID.randomUUID()), null,
                new PostContent("Content"), "Can Tho", PostVisibility.PUBLIC,
                List.of(media), Set.of(cropTypeId), NOW, 10);

        JpaPostEntity entity = postMapper.toEntity(post);
        var mediaEntities = postMapper.toMediaEntities(post);
        var cropTypeEntities = postMapper.toCropTypeEntities(post);
        Post reconstructed = postMapper.toDomain(entity, mediaEntities, cropTypeEntities,
                new PostLimitsProperties(2_000, 10, 50));

        assertThat(entity.getTopicId()).isNull();
        assertThat(entity.getVisibility()).isEqualTo("PUBLIC");
        assertThat(mediaEntities).singleElement().satisfies(value -> {
            assertThat(value.getPostId()).isEqualTo(post.getId().value());
            assertThat(value.getMediaType()).isEqualTo("IMAGE");
        });
        assertThat(cropTypeEntities).singleElement().satisfies(value ->
                assertThat(value.getId().getCropTypeId()).isEqualTo(cropTypeId.value()));
        assertThat(reconstructed.getContent()).isEqualTo(post.getContent());
        assertThat(reconstructed.getMedia()).hasSize(1);
        assertThat(reconstructed.getCropTypeIds()).containsExactly(cropTypeId);
    }

    @Test
    void mapsPostWithoutTypeRoundTrip() {
        Post post = Post.createPublished(new PostId(UUID.randomUUID()),
                new AuthorUserId(UUID.randomUUID()), null, null,
                new PostContent("Content"), null, PostVisibility.PUBLIC,
                List.of(), Set.of(), NOW, 10);

        JpaPostEntity entity = postMapper.toEntity(post);
        Post reconstructed = postMapper.toDomain(
                entity, List.of(), List.of(), new PostLimitsProperties(2_000, 10, 50));

        assertThat(entity.getPostTypeId()).isNull();
        assertThat(reconstructed.getPostTypeId()).isNull();
    }
}
