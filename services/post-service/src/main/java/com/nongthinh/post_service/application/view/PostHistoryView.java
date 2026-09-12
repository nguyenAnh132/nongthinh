package com.nongthinh.post_service.application.view;

import com.nongthinh.post_service.domain.history.PostHistory;
import com.nongthinh.post_service.domain.history.valueobject.HistoryActorType;
import com.nongthinh.post_service.domain.history.valueobject.PostHistoryAction;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import java.time.Instant;
import java.util.UUID;

public record PostHistoryView(
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
        PostSnapshotView snapshot,
        Instant createdAt
) {
    public static PostHistoryView from(PostHistory history) {
        return new PostHistoryView(
                history.getId(),
                history.getPostId(),
                history.getPostAuthorUserId(),
                history.getActorUserId(),
                history.getActorType(),
                history.getAction(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getPreviousVisibility(),
                history.getNewVisibility(),
                history.getReasonCode(),
                history.getReasonDetail(),
                history.getReportId(),
                PostSnapshotView.from(history.getSnapshot()),
                history.getCreatedAt()
        );
    }
}
