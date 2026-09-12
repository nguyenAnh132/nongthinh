package com.nongthinh.post_service.application.port.in.postreport.impl;

import com.nongthinh.post_service.application.port.in.postreport.GetPostReportUseCase;
import com.nongthinh.post_service.application.service.PostReportUseCaseSupport;
import com.nongthinh.post_service.application.view.PostReportView;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPostReportUseCaseImpl implements GetPostReportUseCase {
    private final PostReportUseCaseSupport support;

    @Override
    public PostReportView execute(UUID reportId) {
        return PostReportView.from(support.requireReport(reportId));
    }
}
