package com.nongthinh.post_service.presentation.dto.request;

import com.nongthinh.post_service.domain.report.valueobject.ReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePostReportRequest(
        @NotNull(message = "POST_REPORT_REASON_REQUIRED")
        ReportReason reason,

        @Pattern(regexp = "(?s).*\\S.*", message = "POST_REPORT_REASON_DETAIL_INVALID")
        @Size(max = 10_000, message = "POST_REPORT_REASON_DETAIL_TOO_LONG")
        String reasonDetail
) {
}
