package com.nongthinh.post_service.application.view;

import com.nongthinh.post_service.domain.interaction.valueobject.ReactionType;
import java.util.Map;
import java.util.UUID;

public record PostEngagementView(UUID postId, Map<ReactionType, Long> reactionCounts,
        long reactionTotal, long reactionVersion, ReactionType currentReaction,
        long commentRootTotal, long commentTotal, long commentVersion) {
    public PostEngagementView {
        reactionCounts = Map.copyOf(reactionCounts);
    }
}
