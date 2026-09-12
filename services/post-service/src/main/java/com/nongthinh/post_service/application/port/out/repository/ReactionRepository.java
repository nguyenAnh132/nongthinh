package com.nongthinh.post_service.application.port.out.repository;

import com.nongthinh.post_service.domain.interaction.Reaction;
import com.nongthinh.post_service.application.model.ReactionPage;
import com.nongthinh.post_service.domain.interaction.valueobject.ReactionType;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface ReactionRepository {
    ReactionPage findByPostId(UUID postId, ReactionType reactionType, int page, int size);
    Optional<Reaction> findByPostIdAndActorId(UUID postId, UUID actorId);
    Map<ReactionType, Long> countByPostId(UUID postId);
    Reaction upsert(Reaction reaction);
    void deleteByPostIdAndActorId(UUID postId, UUID actorId);
}
