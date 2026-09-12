package com.nongthinh.post_service.application.port.in.postreport;

import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostReportView;
import com.nongthinh.post_service.domain.report.valueobject.ReportStatus;

public interface ListPostReportsUseCase {
    PageView<PostReportView> execute(ReportStatus status, int page, int size);
}
