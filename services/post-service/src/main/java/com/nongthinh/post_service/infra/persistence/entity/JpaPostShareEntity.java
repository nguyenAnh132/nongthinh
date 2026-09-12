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
@Table(name = "post_shares")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JpaPostShareEntity {
    @Id private UUID id;
    @Column(name = "post_id", nullable = false) private UUID postId;
    @Column(name = "shared_by_user_id", nullable = false) private UUID sharedByUserId;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
}
