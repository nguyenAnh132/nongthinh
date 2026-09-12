package com.nongthinh.post_service.domain.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nongthinh.post_service.domain.post.valueobject.AuthorUserId;
import com.nongthinh.post_service.domain.post.valueobject.CropTypeId;
import com.nongthinh.post_service.domain.post.valueobject.FileId;
import com.nongthinh.post_service.domain.post.valueobject.MediaType;
import com.nongthinh.post_service.domain.post.valueobject.PostContent;
import com.nongthinh.post_service.domain.post.valueobject.PostId;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostTypeId;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PostTest {
    private static final Instant NOW = Instant.parse("2026-08-21T00:00:00Z");
    private static final AuthorUserId AUTHOR = new AuthorUserId(UUID.randomUUID());

    @Test
    void createsPublishedPostWithNormalizedContentAndSnapshotMedia() {
        Post post = Post.createPublished(new PostId(UUID.randomUUID()), AUTHOR,
                new PostTypeId(UUID.randomUUID()), null, new PostContent("  Nội dung  "),
                "  Cần Thơ  ", PostVisibility.PUBLIC, List.of(media(0, UUID.randomUUID())),
                Set.of(new CropTypeId(UUID.randomUUID())), NOW, 10);

        assertThat(post.getContent().value()).isEqualTo("Nội dung");
        assertThat(post.getLocationText()).isEqualTo("Cần Thơ");
        assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(post.getPublishedAt()).isEqualTo(NOW);
        assertThat(post.isVisibleInPublicFeed()).isTrue();
    }

    @Test
    void draftHasNoPublishedTimeUntilAuthorPublishesIt() {
        Post post = Post.createDraft(new PostId(UUID.randomUUID()), AUTHOR,
                new PostTypeId(UUID.randomUUID()), null, new PostContent("Nội dung"), null,
                PostVisibility.PUBLIC, List.of(), Set.of(), NOW, 10);

        post.publish(AUTHOR, NOW.plusSeconds(60));

        assertThat(post.getStatus()).isEqualTo(PostStatus.PUBLISHED);
        assertThat(post.getPublishedAt()).isEqualTo(NOW.plusSeconds(60));
    }

    @Test
    void supportsPostsWithoutTypeAndAllowsClearingAnExistingType() {
        Post post = Post.createPublished(new PostId(UUID.randomUUID()), AUTHOR,
                null, null, new PostContent("Nội dung"), null,
                PostVisibility.PUBLIC, List.of(), Set.of(), NOW, 10);

        assertThat(post.getPostTypeId()).isNull();

        post.edit(AUTHOR, new PostTypeId(UUID.randomUUID()), null,
                new PostContent("Nội dung đã sửa"), null, PostVisibility.PUBLIC,
                List.of(), Set.of(), 10, NOW.plusSeconds(1));
        assertThat(post.getPostTypeId()).isNotNull();

        post.edit(AUTHOR, null, null, new PostContent("Nội dung đã bỏ loại"), null,
                PostVisibility.PUBLIC, List.of(), Set.of(), 10, NOW.plusSeconds(2));
        assertThat(post.getPostTypeId()).isNull();
    }

    @Test
    void rejectsEditFromNonOwner() {
        Post post = publishedPost(List.of());

        assertThatThrownBy(() -> post.changeVisibility(
                new AuthorUserId(UUID.randomUUID()), PostVisibility.PRIVATE, NOW.plusSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("author");
    }

    @Test
    void rejectsDuplicateFileAndDisplayOrder() {
        UUID fileId = UUID.randomUUID();
        assertThatThrownBy(() -> publishedPost(List.of(media(0, fileId), media(1, fileId))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fileId");

        assertThatThrownBy(() -> publishedPost(List.of(
                media(0, UUID.randomUUID()), media(0, UUID.randomUUID()))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("displayOrder");
    }

    @Test
    void hiddenAndDeletedPostsAreNotPubliclyVisible() {
        Post hidden = publishedPost(List.of());
        hidden.hide(NOW.plusSeconds(1));
        assertThat(hidden.isVisibleInPublicFeed()).isFalse();
        hidden.restore(NOW.plusSeconds(2));
        assertThat(hidden.isVisibleInPublicFeed()).isTrue();
        hidden.softDelete(AUTHOR, NOW.plusSeconds(3));
        assertThat(hidden.isVisibleInPublicFeed()).isFalse();
    }

    @Test
    void enforcesContentAndMediaLimits() {
        assertThatThrownBy(() -> new PostContent(" ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PostContent("x".repeat(2_001))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> publishedPost(List.of(media(0, UUID.randomUUID())) , 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("configured limit");
    }

    @Test
    void failedEditDoesNotPartiallyChangePost() {
        Post post = publishedPost(List.of());
        PostTypeId originalType = post.getPostTypeId();
        PostContent originalContent = post.getContent();

        assertThatThrownBy(() -> post.edit(AUTHOR, new PostTypeId(UUID.randomUUID()), null,
                new PostContent("Changed"), "Hanoi", PostVisibility.PRIVATE,
                List.of(media(0, UUID.randomUUID())), Set.of(), 0, NOW.plusSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("configured limit");

        assertThat(post.getPostTypeId()).isEqualTo(originalType);
        assertThat(post.getContent()).isEqualTo(originalContent);
        assertThat(post.getVisibility()).isEqualTo(PostVisibility.PUBLIC);
    }

    @Test
    void rejectsChangesWithTimeBeforeLastUpdate() {
        Post post = publishedPost(List.of());
        post.changeVisibility(AUTHOR, PostVisibility.PRIVATE, NOW.plusSeconds(2));

        assertThatThrownBy(() -> post.changeVisibility(
                AUTHOR, PostVisibility.PUBLIC, NOW.plusSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("updatedAt");

        assertThat(post.getVisibility()).isEqualTo(PostVisibility.PRIVATE);
    }

    @Test
    void authorCanAddReplaceAndRemoveMedia() {
        Post post = publishedPost(List.of());
        UUID mediaId = UUID.randomUUID();
        PostMedia original = PostMedia.create(mediaId, new FileId(UUID.randomUUID()),
                MediaType.IMAGE, "https://cdn.example.test/one.jpg", "image/jpeg",
                null, null, 100L, 0, null, NOW);
        post.addMedia(AUTHOR, original, 10, NOW.plusSeconds(1));

        PostMedia replacement = original.updateSnapshot(
                new FileId(UUID.randomUUID()), MediaType.IMAGE,
                "https://cdn.example.test/two.jpg", "image/webp", null, null,
                200L, 1, "Updated");
        post.replaceMedia(AUTHOR, mediaId, replacement, 10, NOW.plusSeconds(2));

        assertThat(post.getMedia()).singleElement().satisfies(item -> {
            assertThat(item.getId()).isEqualTo(mediaId);
            assertThat(item.getDisplayOrder()).isEqualTo(1);
            assertThat(item.getCaption()).isEqualTo("Updated");
        });

        post.removeMedia(AUTHOR, mediaId, 10, NOW.plusSeconds(3));
        assertThat(post.getMedia()).isEmpty();
    }

    private static Post publishedPost(List<PostMedia> media) { return publishedPost(media, 10); }

    private static Post publishedPost(List<PostMedia> media, int maxMedia) {
        return Post.createPublished(new PostId(UUID.randomUUID()), AUTHOR,
                new PostTypeId(UUID.randomUUID()), null, new PostContent("Nội dung"), null,
                PostVisibility.PUBLIC, media, Set.of(), NOW, maxMedia);
    }

    private static PostMedia media(int order, UUID fileId) {
        return PostMedia.create(UUID.randomUUID(), new FileId(fileId), MediaType.IMAGE,
                "https://cdn.example.test/post.jpg", "image/jpeg", 800, 600, 10_000L,
                order, null, NOW);
    }
}
