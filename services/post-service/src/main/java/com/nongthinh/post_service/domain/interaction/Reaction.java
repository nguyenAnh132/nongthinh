package com.nongthinh.post_service.domain.interaction;

import static com.nongthinh.post_service.domain.shared.DomainValidation.required;
import static com.nongthinh.post_service.domain.shared.DomainValidation.requiredId;
import static com.nongthinh.post_service.domain.shared.DomainValidation.notBefore;

import com.nongthinh.post_service.domain.interaction.valueobject.ReactionType;
import java.time.Instant;
import java.util.UUID;

public final class Reaction {
    private final UUID id;
    private final UUID postId;
    private final UUID actorId;
    private ReactionType type;
    private final Instant createdAt;
    private Instant updatedAt;

    private Reaction(UUID id, UUID postId, UUID actorId, ReactionType type,
                     Instant createdAt, Instant updatedAt) {
        this.id = requiredId(id, "reactionId");
        this.postId = requiredId(postId, "postId");
        this.actorId = requiredId(actorId, "actorId");
        this.type = required(type, "reactionType");
        this.createdAt = required(createdAt, "createdAt");
        this.updatedAt = required(updatedAt, "updatedAt");
        notBefore(this.updatedAt, this.createdAt, "updatedAt", "createdAt");
    }

    public static Reaction create(UUID id, UUID postId, UUID actorId, ReactionType type, Instant now) {
        return new Reaction(id, postId, actorId, type, now, now);
    }

    public static Reaction reconstruct(UUID id, UUID postId, UUID actorId, ReactionType type,
                                       Instant createdAt, Instant updatedAt) {
        return new Reaction(id, postId, actorId, type, createdAt, updatedAt);
    }

    public void changeTo(ReactionType type, Instant now) {
        ReactionType validatedType = required(type, "reactionType");
        Instant changedAt = notBefore(now, updatedAt, "now", "updatedAt");
        this.type = validatedType;
        this.updatedAt = changedAt;
    }

    public UUID getId() { return id; }
    public UUID getPostId() { return postId; }
    public UUID getActorId() { return actorId; }
    public ReactionType getType() { return type; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
