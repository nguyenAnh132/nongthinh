package com.nongthinh.post_service.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_reactions")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JpaPostReactionEntity {
    @Id private UUID id;
    @Column(name = "post_id", nullable = false) private UUID postId;
    @Column(name = "actor_id", nullable = false) private UUID actorId;
    @Column(name = "reaction_type", nullable = false, length = 30) private String reactionType;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}
