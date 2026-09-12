package com.nongthinh.post_service.application.service;

import com.nongthinh.post_service.application.port.out.repository.PostReportRepository;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.configuration.PostLimitsProperties;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.report.PostReport;
import com.nongthinh.post_service.domain.report.valueobject.ReportStatus;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostReportUseCaseSupport {
    private static final int TEXT_MAX_LENGTH = 10_000;

    private final PostReportRepository repository;
    private final PostLimitsProperties limits;

    public PostReport requireReport(UUID reportId) {
        return repository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_REPORT_NOT_FOUND));
    }

    public PostReport requireReportForUpdate(UUID reportId) {
        return repository.findByIdForUpdate(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_REPORT_NOT_FOUND));
    }

    public void assertPending(PostReport report) {
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new BusinessException(ErrorCode.POST_REPORT_STATUS_CONFLICT);
        }
    }

    public void assertCompletable(PostReport report) {
        if (report.getStatus() != ReportStatus.PENDING
                && report.getStatus() != ReportStatus.UNDER_REVIEW) {
            throw new BusinessException(ErrorCode.POST_REPORT_STATUS_CONFLICT);
        }
    }

    public String reasonDetail(String value) {
        return normalizeOptionalText(
                value,
                ErrorCode.POST_REPORT_REASON_DETAIL_INVALID,
                ErrorCode.POST_REPORT_REASON_DETAIL_TOO_LONG
        );
    }

    public String resolutionNote(String value) {
        return normalizeOptionalText(
                value,
                ErrorCode.POST_REPORT_RESOLUTION_NOTE_INVALID,
                ErrorCode.POST_REPORT_RESOLUTION_NOTE_TOO_LONG
        );
    }

    public void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > limits.pageSize()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }
    }

    private String normalizeOptionalText(String value, ErrorCode blankCode, ErrorCode tooLongCode) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new BusinessException(blankCode);
        }
        if (normalized.length() > TEXT_MAX_LENGTH) {
            throw new BusinessException(tooLongCode);
        }
        return normalized;
    }
}
