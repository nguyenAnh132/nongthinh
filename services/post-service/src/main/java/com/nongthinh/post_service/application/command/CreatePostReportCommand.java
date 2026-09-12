package com.nongthinh.post_service.application.command;

import com.nongthinh.post_service.domain.report.valueobject.ReportReason;

public record CreatePostReportCommand(
        ReportReason reason,
        String reasonDetail
) {
}
