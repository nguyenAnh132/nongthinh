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
@Table(name = "posts")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JpaPostEntity {
    @Id private UUID id;
    @Column(name = "reaction_version", insertable = false, updatable = false) private long reactionVersion;
    @Column(name = "comment_version", insertable = false, updatable = false) private long commentVersion;
    @Column(name = "author_user_id", nullable = false) private UUID authorUserId;
    @Column(name = "post_type_id") private UUID postTypeId;
    @Column(name = "topic_id") private UUID topicId;
    @Column(name = "location_text", length = 120) private String locationText;
    @Column(nullable = false, length = 2_000) private String content;
    @Column(nullable = false, length = 20) private String visibility;
    @Column(nullable = false, length = 30) private String status;
    @Column(name = "published_at") private Instant publishedAt;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Column(name = "deleted_at") private Instant deletedAt;
}
