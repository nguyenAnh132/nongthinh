package com.nongthinh.post_service.application.port.in.postreport.impl;

import com.nongthinh.post_service.application.command.CompletePostReportCommand;
import com.nongthinh.post_service.application.port.in.postreport.RejectPostReportUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.repository.PostReportRepository;
import com.nongthinh.post_service.application.service.PostReportUseCaseSupport;
import com.nongthinh.post_service.application.service.PostReportNotificationRecorder;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PostReportView;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.report.PostReport;
import com.nongthinh.post_service.domain.post.Post;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RejectPostReportUseCaseImpl implements RejectPostReportUseCase {
    private final PostReportRepository repository;
    private final PostReportUseCaseSupport support;
    private final PostUseCaseSupport postSupport;
    private final PostReportNotificationRecorder notificationRecorder;
    private final CurrentUserProvider currentUserProvider;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public PostReportView execute(UUID reportId, CompletePostReportCommand command) {
        if (command == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }
        PostReport report = support.requireReportForUpdate(reportId);
        support.assertCompletable(report);
        Post post = postSupport.requirePostIncludingDeleted(report.getPostId());
        UUID moderatorId = currentUserProvider.getCurrentUserId();
        Instant now = clockProvider.now();
        report.reject(moderatorId, support.resolutionNote(command.resolutionNote()), now);
        PostReport saved = repository.save(report);
        notificationRecorder.recordRejected(saved, post, moderatorId, now);
        return PostReportView.from(saved);
    }
}
