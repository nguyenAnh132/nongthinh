package com.nongthinh.post_service.domain.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nongthinh.post_service.domain.post.valueobject.FileId;
import com.nongthinh.post_service.domain.post.valueobject.MediaType;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PostMediaTest {
    private static final Instant NOW = Instant.parse("2026-08-21T00:00:00Z");

    @Test
    void createsValidatedImmutableMediaSnapshot() {
        PostMedia media = PostMedia.create(UUID.randomUUID(), new FileId(UUID.randomUUID()),
                MediaType.IMAGE, "  https://cdn.example.test/image.jpg  ", " image/jpeg ",
                800, 600, 10_000L, 0, "  Rice field  ", NOW);

        assertThat(media.getMediaUrl()).isEqualTo("https://cdn.example.test/image.jpg");
        assertThat(media.getContentType()).isEqualTo("image/jpeg");
        assertThat(media.getCaption()).isEqualTo("Rice field");
    }

    @Test
    void rejectsMismatchedTypeAndIncompleteDimensions() {
        assertThatThrownBy(() -> PostMedia.create(UUID.randomUUID(),
                new FileId(UUID.randomUUID()), MediaType.VIDEO,
                "https://cdn.example.test/image.jpg", "image/jpeg",
                800, 600, 10_000L, 0, null, NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mediaType");

        assertThatThrownBy(() -> PostMedia.create(UUID.randomUUID(),
                new FileId(UUID.randomUUID()), MediaType.IMAGE,
                "https://cdn.example.test/image.jpg", "image/jpeg",
                800, null, 10_000L, 0, null, NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("width and height");
    }
}
