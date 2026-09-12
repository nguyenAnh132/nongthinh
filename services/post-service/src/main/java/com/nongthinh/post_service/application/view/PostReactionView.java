package com.nongthinh.post_service.application.view;

import com.nongthinh.post_service.domain.interaction.Reaction;
import com.nongthinh.post_service.domain.interaction.valueobject.ReactionType;
import java.time.Instant;
import java.util.UUID;

public record PostReactionView(
        UUID id,
        UUID postId,
        UUID actorId,
        ReactionType reactionType,
        Instant createdAt,
        Instant updatedAt,
        ReactionType currentReaction,
        java.util.Map<ReactionType, Long> reactionCounts,
        long reactionTotal,
        long reactionVersion
) {
    public PostReactionView(UUID id, UUID postId, UUID actorId, ReactionType reactionType,
                            Instant createdAt, Instant updatedAt) {
        this(id, postId, actorId, reactionType, createdAt, updatedAt, reactionType, java.util.Map.of(), 0, 0);
    }

    public PostReactionView withMetrics(PostEngagementView metrics) {
        return new PostReactionView(id, postId, actorId, reactionType, createdAt, updatedAt,
                metrics.currentReaction(), metrics.reactionCounts(), metrics.reactionTotal(), metrics.reactionVersion());
    }
    public static PostReactionView from(Reaction reaction) {
        return new PostReactionView(
                reaction.getId(), reaction.getPostId(), reaction.getActorId(),
                reaction.getType(), reaction.getCreatedAt(), reaction.getUpdatedAt()
        );
    }
}
