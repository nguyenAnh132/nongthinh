package com.nongthinh.post_service.presentation.dto.request;

import com.nongthinh.post_service.domain.report.valueobject.PostModerationAction;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompletePostReportRequest(
        @Pattern(regexp = "(?s).*\\S.*", message = "POST_REPORT_RESOLUTION_NOTE_INVALID")
        @Size(max = 10_000, message = "POST_REPORT_RESOLUTION_NOTE_TOO_LONG")
        String resolutionNote,
        PostModerationAction moderationAction
) {
}
