package com.nongthinh.post_service.application.port.in.postreport.impl;

import com.nongthinh.post_service.application.model.PostReportPage;
import com.nongthinh.post_service.application.port.in.postreport.ListPostReportsUseCase;
import com.nongthinh.post_service.application.port.out.repository.PostReportRepository;
import com.nongthinh.post_service.application.service.PostReportUseCaseSupport;
import com.nongthinh.post_service.application.view.PageView;
import com.nongthinh.post_service.application.view.PostReportView;
import com.nongthinh.post_service.domain.report.valueobject.ReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListPostReportsUseCaseImpl implements ListPostReportsUseCase {
    private final PostReportRepository repository;
    private final PostReportUseCaseSupport support;

    @Override
    public PageView<PostReportView> execute(ReportStatus status, int page, int size) {
        support.validatePage(page, size);
        PostReportPage result = repository.findAll(status, page, size);
        return new PageView<>(
                result.items().stream().map(PostReportView::from).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        );
    }
}
