package com.nongthinh.post_service.application.model;

import com.nongthinh.post_service.domain.report.PostReport;
import java.util.List;

public record PostReportPage(
        List<PostReport> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public PostReportPage {
        items = List.copyOf(items);
    }
}
