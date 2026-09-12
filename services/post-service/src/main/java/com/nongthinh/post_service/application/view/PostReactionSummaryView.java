package com.nongthinh.post_service.application.view;

import com.nongthinh.post_service.domain.interaction.valueobject.ReactionType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public record PostReactionSummaryView(
        long totalCount,
        Map<ReactionType, Long> counts,
        ReactionType currentUserReaction,
        long reactionVersion
) {
    public PostReactionSummaryView(long totalCount, Map<ReactionType, Long> counts, ReactionType currentUserReaction) {
        this(totalCount, counts, currentUserReaction, 0);
    }
    public PostReactionSummaryView {
        counts = counts == null ? Map.of() : counts;
        EnumMap<ReactionType, Long> normalized = new EnumMap<>(ReactionType.class);
        for (ReactionType type : ReactionType.values()) {
            normalized.put(type, counts.getOrDefault(type, 0L));
        }
        counts = Collections.unmodifiableMap(normalized);
        totalCount = normalized.values().stream().mapToLong(Long::longValue).sum();
    }
}
