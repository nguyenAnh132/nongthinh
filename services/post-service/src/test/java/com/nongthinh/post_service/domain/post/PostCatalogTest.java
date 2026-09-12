package com.nongthinh.post_service.domain.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PostCatalogTest {
    private static final Instant NOW = Instant.parse("2026-08-21T00:00:00Z");

    @Test
    void validatesStableCatalogIdentifiers() {
        assertThatThrownBy(() -> PostType.create(UUID.randomUUID(), "question",
                "Question", null, 0, NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("uppercase snake case");
        assertThatThrownBy(() -> PostTopic.create(UUID.randomUUID(), "Rice",
                "Rice Topic", null, 0, NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lowercase kebab case");
    }

    @Test
    void failedCatalogUpdateDoesNotPartiallyChangeEntity() {
        PostType type = PostType.create(UUID.randomUUID(), "QUESTION", "Question",
                "Description", 1, NOW);

        assertThatThrownBy(() -> type.update("Changed", "New description", -1,
                NOW.plusSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("displayOrder");

        assertThat(type.getName()).isEqualTo("Question");
        assertThat(type.getDescription()).isEqualTo("Description");
        assertThat(type.getDisplayOrder()).isEqualTo(1);
    }
}
