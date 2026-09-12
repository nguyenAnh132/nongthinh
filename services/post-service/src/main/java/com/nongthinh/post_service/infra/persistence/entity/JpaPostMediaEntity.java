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
@Table(name = "post_media")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JpaPostMediaEntity {
    @Id private UUID id;
    @Column(name = "post_id", nullable = false) private UUID postId;
    @Column(name = "file_id", nullable = false) private UUID fileId;
    @Column(name = "media_type", nullable = false, length = 30) private String mediaType;
    @Column(name = "media_url", nullable = false, length = 1_000) private String mediaUrl;
    @Column(name = "content_type", nullable = false, length = 100) private String contentType;
    private Integer width;
    private Integer height;
    @Column(name = "size_bytes") private Long sizeBytes;
    @Column(name = "display_order", nullable = false) private int displayOrder;
    @Column(length = 500) private String caption;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
}
