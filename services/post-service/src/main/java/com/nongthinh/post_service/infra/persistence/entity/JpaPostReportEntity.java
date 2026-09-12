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
@Table(name = "post_reports")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JpaPostReportEntity {
    @Id private UUID id;
    @Column(name = "post_id", nullable = false) private UUID postId;
    @Column(name = "reporter_id", nullable = false) private UUID reporterId;
    @Column(name = "reason_code", nullable = false, length = 50) private String reasonCode;
    @Column(name = "reason_detail", columnDefinition = "TEXT") private String reasonDetail;
    @Column(nullable = false, length = 30) private String status;
    @Column(name = "resolved_by") private UUID resolvedBy;
    @Column(name = "resolved_at") private Instant resolvedAt;
    @Column(name = "resolution_note", columnDefinition = "TEXT") private String resolutionNote;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}
