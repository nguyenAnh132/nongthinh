package com.nongthinh.post_service.infra.persistence.entity;

import com.nongthinh.post_service.domain.history.PostSnapshot;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "post_histories")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JpaPostHistoryEntity {
    @Id private UUID id;
    @Column(name = "post_id", nullable = false) private UUID postId;
    @Column(name = "post_author_user_id", nullable = false) private UUID postAuthorUserId;
    @Column(name = "actor_user_id") private UUID actorUserId;
    @Column(name = "actor_type", nullable = false, length = 20) private String actorType;
    @Column(nullable = false, length = 40) private String action;
    @Column(name = "previous_status", length = 30) private String previousStatus;
    @Column(name = "new_status", length = 30) private String newStatus;
    @Column(name = "previous_visibility", length = 20) private String previousVisibility;
    @Column(name = "new_visibility", length = 20) private String newVisibility;
    @Column(name = "reason_code", length = 50) private String reasonCode;
    @Column(name = "reason_detail", columnDefinition = "TEXT") private String reasonDetail;
    @Column(name = "report_id") private UUID reportId;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_data", nullable = false, columnDefinition = "jsonb")
    private PostSnapshot snapshotData;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
}
