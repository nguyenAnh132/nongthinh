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
@Table(name = "post_comments")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JpaPostCommentEntity {
    @Id private UUID id;
    @Column(name = "post_id", nullable = false) private UUID postId;
    @Column(name = "parent_comment_id") private UUID parentCommentId;
    @Column(name = "author_user_id", nullable = false) private UUID authorUserId;
    @Column(nullable = false, length = 2_000) private String content;
    @Column(nullable = false, length = 30) private String status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Column(name = "deleted_at") private Instant deletedAt;
}
