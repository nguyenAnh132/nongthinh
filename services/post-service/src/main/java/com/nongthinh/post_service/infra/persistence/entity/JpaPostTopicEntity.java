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
@Table(name = "post_topics")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JpaPostTopicEntity {
    @Id private UUID id;
    @Column(nullable = false, length = 150) private String name;
    @Column(nullable = false, length = 180) private String slug;
    @Column(length = 500) private String description;
    @Column(name = "display_order", nullable = false) private int displayOrder;
    @Column(name = "is_active", nullable = false) private boolean active;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}
