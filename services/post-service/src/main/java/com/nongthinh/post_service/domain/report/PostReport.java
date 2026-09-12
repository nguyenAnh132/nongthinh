package com.nongthinh.post_service.domain.report;

import static com.nongthinh.post_service.domain.shared.DomainValidation.optionalText;
import static com.nongthinh.post_service.domain.shared.DomainValidation.required;
import static com.nongthinh.post_service.domain.shared.DomainValidation.requiredId;
import static com.nongthinh.post_service.domain.shared.DomainValidation.notBefore;

import com.nongthinh.post_service.domain.report.valueobject.ReportReason;
import com.nongthinh.post_service.domain.report.valueobject.ReportStatus;
import java.time.Instant;
import java.util.UUID;

public final class PostReport {
    private final UUID id;
    private final UUID postId;
    private final UUID reporterId;
    private final ReportReason reason;
    private final String reasonDetail;
    private ReportStatus status;
    private UUID resolvedBy;
    private Instant resolvedAt;
    private String resolutionNote;
    private final Instant createdAt;
    private Instant updatedAt;

    private PostReport(UUID id, UUID postId, UUID reporterId, ReportReason reason,
                       String reasonDetail, ReportStatus status, UUID resolvedBy,
                       Instant resolvedAt, String resolutionNote, Instant createdAt,
                       Instant updatedAt) {
        this.id = requiredId(id, "reportId");
        this.postId = requiredId(postId, "postId");
        this.reporterId = requiredId(reporterId, "reporterId");
        this.reason = required(reason, "reason");
        this.reasonDetail = optionalText(reasonDetail, 10_000, "reasonDetail");
        this.status = required(status, "status");
        this.resolvedBy = resolvedBy;
        this.resolvedAt = resolvedAt;
        this.resolutionNote = resolutionNote == null ? null : optionalText(resolutionNote, 10_000, "resolutionNote");
        this.createdAt = required(createdAt, "createdAt");
        this.updatedAt = required(updatedAt, "updatedAt");
        notBefore(this.updatedAt, this.createdAt, "updatedAt", "createdAt");
        validateResolution();
    }

    public static PostReport create(UUID id, UUID postId, UUID reporterId, ReportReason reason,
                                    String reasonDetail, Instant now) {
        return new PostReport(id, postId, reporterId, reason, reasonDetail, ReportStatus.PENDING,
                null, null, null, now, now);
    }

    public static PostReport reconstruct(UUID id, UUID postId, UUID reporterId, ReportReason reason,
                                         String reasonDetail, ReportStatus status, UUID resolvedBy,
                                         Instant resolvedAt, String resolutionNote, Instant createdAt,
                                         Instant updatedAt) {
        return new PostReport(id, postId, reporterId, reason, reasonDetail, status, resolvedBy,
                resolvedAt, resolutionNote, createdAt, updatedAt);
    }

    public void startReview(Instant now) {
        if (status != ReportStatus.PENDING) {
            throw new IllegalStateException("Only a pending report can enter review");
        }
        Instant changedAt = changeTime(now);
        status = ReportStatus.UNDER_REVIEW;
        updatedAt = changedAt;
    }

    public void resolve(UUID moderatorId, String note, Instant now) {
        complete(ReportStatus.RESOLVED, moderatorId, note, now);
    }

    public void reject(UUID moderatorId, String note, Instant now) {
        complete(ReportStatus.REJECTED, moderatorId, note, now);
    }

    private void complete(ReportStatus target, UUID moderatorId, String note, Instant now) {
        if (status != ReportStatus.PENDING && status != ReportStatus.UNDER_REVIEW) {
            throw new IllegalStateException("A completed report cannot be resolved again");
        }
        UUID validatedModeratorId = requiredId(moderatorId, "moderatorId");
        String validatedNote = optionalText(note, 10_000, "resolutionNote");
        Instant changedAt = changeTime(now);
        status = target;
        resolvedBy = validatedModeratorId;
        resolvedAt = changedAt;
        resolutionNote = validatedNote;
        updatedAt = changedAt;
    }

    private void validateResolution() {
        boolean completed = status == ReportStatus.RESOLVED || status == ReportStatus.REJECTED;
        if (completed) {
            requiredId(resolvedBy, "resolvedBy");
            notBefore(resolvedAt, createdAt, "resolvedAt", "createdAt");
            notBefore(updatedAt, resolvedAt, "updatedAt", "resolvedAt");
        } else if (resolvedBy != null || resolvedAt != null || resolutionNote != null) {
            throw new IllegalArgumentException("An open report cannot have resolution data");
        }
    }

    private Instant changeTime(Instant now) {
        return notBefore(now, updatedAt, "now", "updatedAt");
    }

    public UUID getId() { return id; }
    public UUID getPostId() { return postId; }
    public UUID getReporterId() { return reporterId; }
    public ReportReason getReason() { return reason; }
    public String getReasonDetail() { return reasonDetail; }
    public ReportStatus getStatus() { return status; }
    public UUID getResolvedBy() { return resolvedBy; }
    public Instant getResolvedAt() { return resolvedAt; }
    public String getResolutionNote() { return resolutionNote; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
