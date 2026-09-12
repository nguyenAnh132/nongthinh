package com.nongthinh.post_service.application.view;

import com.nongthinh.post_service.domain.report.PostReport;
import com.nongthinh.post_service.domain.report.valueobject.ReportReason;
import com.nongthinh.post_service.domain.report.valueobject.ReportStatus;
import java.time.Instant;
import java.util.UUID;

public record PostReportView(
        UUID id,
        UUID postId,
        UUID reporterId,
        ReportReason reason,
        String reasonDetail,
        ReportStatus status,
        UUID resolvedBy,
        Instant resolvedAt,
        String resolutionNote,
        Instant createdAt,
        Instant updatedAt
) {
    public static PostReportView from(PostReport report) {
        return new PostReportView(
                report.getId(),
                report.getPostId(),
                report.getReporterId(),
                report.getReason(),
                report.getReasonDetail(),
                report.getStatus(),
                report.getResolvedBy(),
                report.getResolvedAt(),
                report.getResolutionNote(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}
