package com.nongthinh.post_service.application.service;

import com.nongthinh.post_service.application.event.PostEngagementEvent;
import com.nongthinh.post_service.application.port.out.IdGenerator;
import com.nongthinh.post_service.application.port.out.PostEngagementRepository;
import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.report.PostReport;
import com.nongthinh.post_service.domain.report.valueobject.PostModerationAction;
import com.nongthinh.post_service.domain.report.valueobject.ReportStatus;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostReportNotificationRecorder {
    private final PostEngagementRepository repository;
    private final IdGenerator ids;

    public void recordResolved(PostReport report, Post post, UUID moderatorId,
                               PostModerationAction action, Instant now) {
        String ownerType = action == PostModerationAction.DELETE ? "POST_DELETED" : "POST_HIDDEN";
        append(report, post, moderatorId, report.getReporterId(), "REPORT_RESOLVED", action, now);
        append(report, post, moderatorId, post.getAuthorUserId().value(), ownerType, action, now);
    }

    public void recordRejected(PostReport report, Post post, UUID moderatorId, Instant now) {
        append(report, post, moderatorId, report.getReporterId(), "REPORT_REJECTED", null, now);
        append(report, post, moderatorId, post.getAuthorUserId().value(),
                "POST_REPORT_REJECTED", null, now);
    }

    private void append(PostReport report, Post post, UUID moderatorId, UUID recipientId,
                        String notificationType, PostModerationAction action, Instant now) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("postId", post.getId().value());
        payload.put("reportId", report.getId());
        payload.put("reportStatus", report.getStatus().name());
        payload.put("reason", report.getReason().name());
        if (action != null) payload.put("moderationAction", action.name());
        if (report.getResolutionNote() != null) {
            payload.put("resolutionNote", report.getResolutionNote());
        }
        repository.append(new PostEngagementEvent(
                ids.generate(),
                report.getStatus() == ReportStatus.RESOLVED
                        ? "post.report.resolved" : "post.report.rejected",
                1,
                now,
                post.getId().value(),
                Math.max(1L, now.toEpochMilli()),
                Map.copyOf(payload),
                moderatorId,
                recipientId,
                notificationType,
                false
        ));
    }
}
