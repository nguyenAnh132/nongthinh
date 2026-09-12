package com.nongthinh.post_service.domain.interaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nongthinh.post_service.domain.interaction.valueobject.ReactionType;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InteractionTest {
    private static final Instant NOW = Instant.parse("2026-08-21T00:00:00Z");

    @Test
    void changesReactionAtomically() {
        Reaction reaction = Reaction.create(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), ReactionType.LIKE, NOW);

        assertThatThrownBy(() -> reaction.changeTo(ReactionType.LOVE, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("now");

        assertThat(reaction.getType()).isEqualTo(ReactionType.LIKE);
        reaction.changeTo(ReactionType.LOVE, NOW.plusSeconds(1));
        assertThat(reaction.getType()).isEqualTo(ReactionType.LOVE);
    }

    @Test
    void validatesImmutableInteractions() {
        assertThatThrownBy(() -> new Bookmark(null, UUID.randomUUID(), NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("postId");
        assertThatThrownBy(() -> new PostShare(UUID.randomUUID(), UUID.randomUUID(), null, NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sharedByUserId");
    }
}
