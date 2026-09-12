package com.nongthinh.post_service.application.port.in.postreport.impl;

import com.nongthinh.post_service.application.port.in.postreport.StartPostReportReviewUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.repository.PostReportRepository;
import com.nongthinh.post_service.application.service.PostReportUseCaseSupport;
import com.nongthinh.post_service.application.view.PostReportView;
import com.nongthinh.post_service.domain.report.PostReport;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StartPostReportReviewUseCaseImpl implements StartPostReportReviewUseCase {
    private final PostReportRepository repository;
    private final PostReportUseCaseSupport support;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public PostReportView execute(UUID reportId) {
        PostReport report = support.requireReportForUpdate(reportId);
        support.assertPending(report);
        report.startReview(clockProvider.now());
        return PostReportView.from(repository.save(report));
    }
}
