package com.nongthinh.rice_disease_diagnosis_service.infra.persistence.diagnosishistory;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "diagnosis_image_cleanup_tasks",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_diagnosis_image_cleanup_task_history_file",
                columnNames = {"diagnosis_history_id", "file_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JpaDiagnosisImageCleanupTaskEntity {
    @Id
    private UUID id;

    @Column(name = "diagnosis_history_id", nullable = false)
    private UUID diagnosisHistoryId;

    @Column(name = "file_id", nullable = false)
    private UUID fileId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
