package com.nongthinh.rice_disease_diagnosis_service.domain.diagnosishistory;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A durable, retryable request to remove a private scan image. It deliberately
 * retains no object-storage path, token, or image content.
 */
public final class DiagnosisImageCleanupTask {
    private final UUID id;
    private final UUID diagnosisHistoryId;
    private final UUID fileId;
    private final Instant createdAt;
    private int attempts;
    private Instant lastAttemptAt;
    private Instant completedAt;

    private DiagnosisImageCleanupTask(
            UUID id,
            UUID diagnosisHistoryId,
            UUID fileId,
            Instant createdAt,
            int attempts,
            Instant lastAttemptAt,
            Instant completedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.diagnosisHistoryId = Objects.requireNonNull(diagnosisHistoryId, "diagnosisHistoryId is required");
        this.fileId = Objects.requireNonNull(fileId, "fileId is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        if (attempts < 0) throw new IllegalArgumentException("attempts must not be negative");
        this.attempts = attempts;
        this.lastAttemptAt = lastAttemptAt;
        this.completedAt = completedAt;
    }

    public static DiagnosisImageCleanupTask create(
            UUID id, UUID diagnosisHistoryId, UUID fileId, Instant createdAt) {
        return new DiagnosisImageCleanupTask(id, diagnosisHistoryId, fileId, createdAt, 0, null, null);
    }

    public static DiagnosisImageCleanupTask reconstruct(
            UUID id,
            UUID diagnosisHistoryId,
            UUID fileId,
            Instant createdAt,
            int attempts,
            Instant lastAttemptAt,
            Instant completedAt) {
        return new DiagnosisImageCleanupTask(
                id, diagnosisHistoryId, fileId, createdAt, attempts, lastAttemptAt, completedAt);
    }

    public void complete(Instant completedAt) {
        this.completedAt = Objects.requireNonNull(completedAt, "completedAt is required");
        this.lastAttemptAt = completedAt;
        this.attempts++;
    }

    public void fail(Instant attemptedAt) {
        this.lastAttemptAt = Objects.requireNonNull(attemptedAt, "attemptedAt is required");
        this.attempts++;
    }

    public UUID getId() { return id; }
    public UUID getDiagnosisHistoryId() { return diagnosisHistoryId; }
    public UUID getFileId() { return fileId; }
    public Instant getCreatedAt() { return createdAt; }
    public int getAttempts() { return attempts; }
    public Instant getLastAttemptAt() { return lastAttemptAt; }
    public Instant getCompletedAt() { return completedAt; }
}
