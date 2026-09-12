package com.nongthinh.post_service.domain.history;

import static com.nongthinh.post_service.domain.shared.DomainValidation.optionalText;
import static com.nongthinh.post_service.domain.shared.DomainValidation.required;
import static com.nongthinh.post_service.domain.shared.DomainValidation.requiredId;

import com.nongthinh.post_service.domain.history.valueobject.HistoryActorType;
import com.nongthinh.post_service.domain.history.valueobject.PostHistoryAction;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.time.Instant;
import java.util.UUID;

public final class PostHistory {
    private final UUID id;
    private final UUID postId;
    private final UUID postAuthorUserId;
    private final UUID actorUserId;
    private final HistoryActorType actorType;
    private final PostHistoryAction action;
    private final PostStatus previousStatus;
    private final PostStatus newStatus;
    private final PostVisibility previousVisibility;
    private final PostVisibility newVisibility;
    private final String reasonCode;
    private final String reasonDetail;
    private final UUID reportId;
    private final PostSnapshot snapshot;
    private final Instant createdAt;

    private PostHistory(
            UUID id,
            UUID postId,
            UUID postAuthorUserId,
            UUID actorUserId,
            HistoryActorType actorType,
            PostHistoryAction action,
            PostStatus previousStatus,
            PostStatus newStatus,
            PostVisibility previousVisibility,
            PostVisibility newVisibility,
            String reasonCode,
            String reasonDetail,
            UUID reportId,
            PostSnapshot snapshot,
            Instant createdAt
    ) {
        this.id = requiredId(id, "historyId");
        this.postId = requiredId(postId, "postId");
        this.postAuthorUserId = requiredId(postAuthorUserId, "postAuthorUserId");
        this.actorUserId = actorUserId;
        this.actorType = required(actorType, "actorType");
        this.action = required(action, "action");
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.previousVisibility = previousVisibility;
        this.newVisibility = newVisibility;
        this.reasonCode = optionalText(reasonCode, 50, "reasonCode");
        this.reasonDetail = reasonDetail == null ? null : optionalText(reasonDetail, 10_000, "reasonDetail");
        this.reportId = reportId;
        this.snapshot = required(snapshot, "snapshot");
        this.createdAt = required(createdAt, "createdAt");
        validateActor();
    }

    public static PostHistory create(
            UUID id,
            UUID postId,
            UUID postAuthorUserId,
            UUID actorUserId,
            HistoryActorType actorType,
            PostHistoryAction action,
            PostStatus previousStatus,
            PostStatus newStatus,
            PostVisibility previousVisibility,
            PostVisibility newVisibility,
            String reasonCode,
            String reasonDetail,
            UUID reportId,
            PostSnapshot snapshot,
            Instant now
    ) {
        return new PostHistory(id, postId, postAuthorUserId, actorUserId, actorType, action,
                previousStatus, newStatus, previousVisibility, newVisibility, reasonCode,
                reasonDetail, reportId, snapshot, now);
    }

    public static PostHistory reconstruct(
            UUID id,
            UUID postId,
            UUID postAuthorUserId,
            UUID actorUserId,
            HistoryActorType actorType,
            PostHistoryAction action,
            PostStatus previousStatus,
            PostStatus newStatus,
            PostVisibility previousVisibility,
            PostVisibility newVisibility,
            String reasonCode,
            String reasonDetail,
            UUID reportId,
            PostSnapshot snapshot,
            Instant createdAt
    ) {
        return new PostHistory(id, postId, postAuthorUserId, actorUserId, actorType, action,
                previousStatus, newStatus, previousVisibility, newVisibility, reasonCode,
                reasonDetail, reportId, snapshot, createdAt);
    }

    private void validateActor() {
        if (actorType != HistoryActorType.SYSTEM && actorUserId == null) {
            throw new IllegalArgumentException("A user or admin history entry requires actorUserId");
        }
        if (actorUserId != null) requiredId(actorUserId, "actorUserId");
    }

    public UUID getId() { return id; }
    public UUID getPostId() { return postId; }
    public UUID getPostAuthorUserId() { return postAuthorUserId; }
    public UUID getActorUserId() { return actorUserId; }
    public HistoryActorType getActorType() { return actorType; }
    public PostHistoryAction getAction() { return action; }
    public PostStatus getPreviousStatus() { return previousStatus; }
    public PostStatus getNewStatus() { return newStatus; }
    public PostVisibility getPreviousVisibility() { return previousVisibility; }
    public PostVisibility getNewVisibility() { return newVisibility; }
    public String getReasonCode() { return reasonCode; }
    public String getReasonDetail() { return reasonDetail; }
    public UUID getReportId() { return reportId; }
    public PostSnapshot getSnapshot() { return snapshot; }
    public Instant getCreatedAt() { return createdAt; }
}
