package com.nongthinh.post_service.application.command;

import com.nongthinh.post_service.domain.report.valueobject.PostModerationAction;

public record CompletePostReportCommand(
        String resolutionNote,
        PostModerationAction moderationAction
) {
    public CompletePostReportCommand(String resolutionNote) {
        this(resolutionNote, null);
    }
}
