package com.nongthinh.post_service.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post_bookmarks")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JpaPostBookmarkEntity {
    @EmbeddedId private JpaPostBookmarkId id;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
}
