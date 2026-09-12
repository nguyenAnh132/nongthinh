package com.nongthinh.post_service.application.port.in.postreport.impl;

import com.nongthinh.post_service.application.command.CreatePostReportCommand;
import com.nongthinh.post_service.application.port.in.postreport.CreatePostReportUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.IdGenerator;
import com.nongthinh.post_service.application.port.out.repository.PostReportRepository;
import com.nongthinh.post_service.application.service.PostReportUseCaseSupport;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PostReportView;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.report.PostReport;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatePostReportUseCaseImpl implements CreatePostReportUseCase {
    private final PostReportRepository repository;
    private final PostUseCaseSupport postSupport;
    private final PostReportUseCaseSupport reportSupport;
    private final CurrentUserProvider currentUserProvider;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;

    @Override
    @Transactional
    public PostReportView execute(UUID postId, CreatePostReportCommand command) {
        if (command == null || command.reason() == null) {
            throw new BusinessException(ErrorCode.POST_REPORT_REASON_REQUIRED);
        }
        Post post = postSupport.requireInteractablePost(postId);
        UUID reporterId = currentUserProvider.getCurrentUserId();
        if (post.getAuthorUserId().value().equals(reporterId)) {
            throw new BusinessException(ErrorCode.POST_REPORT_SELF_NOT_ALLOWED);
        }
        if (repository.existsByPostIdAndReporterId(postId, reporterId)) {
            throw new BusinessException(ErrorCode.POST_ALREADY_REPORTED);
        }
        PostReport report = PostReport.create(
                idGenerator.generate(),
                postId,
                reporterId,
                command.reason(),
                reportSupport.reasonDetail(command.reasonDetail()),
                clockProvider.now()
        );
        return PostReportView.from(repository.save(report));
    }
}
