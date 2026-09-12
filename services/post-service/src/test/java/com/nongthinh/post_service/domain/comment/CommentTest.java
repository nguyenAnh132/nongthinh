package com.nongthinh.post_service.domain.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CommentTest {
    private static final Instant NOW = Instant.parse("2026-08-21T00:00:00Z");

    @Test
    void allowsOneReplyLevelOnly() {
        Comment root = Comment.createRoot(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Root", NOW);
        Comment reply = Comment.createReply(UUID.randomUUID(), root, UUID.randomUUID(),
                "Reply", NOW);

        assertThat(reply.getParentCommentId()).isEqualTo(root.getId());
        assertThatThrownBy(() -> Comment.createReply(UUID.randomUUID(), reply,
                UUID.randomUUID(), "Nested reply", NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("one level");
    }

    @Test
    void enforcesModerationTransitions() {
        Comment comment = Comment.createRoot(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), "Root", NOW);

        assertThatThrownBy(() -> comment.restore(NOW.plusSeconds(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hidden");

        comment.hide(NOW.plusSeconds(1));
        assertThatThrownBy(() -> comment.hide(NOW.plusSeconds(2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("published");
    }

    @Test
    void hiddenCommentCannotBeEditedByAuthor() {
        UUID authorId = UUID.randomUUID();
        Comment comment = Comment.createRoot(UUID.randomUUID(), UUID.randomUUID(),
                authorId, "Root", NOW);
        comment.hide(NOW.plusSeconds(1));

        assertThatThrownBy(() -> comment.edit(authorId, "Changed", NOW.plusSeconds(2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hidden");
        assertThat(comment.getContent()).isEqualTo("Root");
    }
}
