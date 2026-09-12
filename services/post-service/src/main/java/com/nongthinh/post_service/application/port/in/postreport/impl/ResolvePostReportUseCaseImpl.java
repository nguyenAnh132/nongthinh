package com.nongthinh.post_service.application.port.in.postreport.impl;

import com.nongthinh.post_service.application.command.CompletePostReportCommand;
import com.nongthinh.post_service.application.port.in.postreport.ResolvePostReportUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.CurrentUserProvider;
import com.nongthinh.post_service.application.port.out.repository.PostRepository;
import com.nongthinh.post_service.application.port.out.repository.PostReportRepository;
import com.nongthinh.post_service.application.service.PostHistoryRecorder;
import com.nongthinh.post_service.application.service.PostReportNotificationRecorder;
import com.nongthinh.post_service.application.service.PostReportUseCaseSupport;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PostReportView;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.exception.BusinessException;
import com.nongthinh.post_service.domain.history.valueobject.PostHistoryAction;
import com.nongthinh.post_service.domain.post.Post;
import com.nongthinh.post_service.domain.post.valueobject.PostStatus;
import com.nongthinh.post_service.domain.post.valueobject.PostVisibility;
import com.nongthinh.post_service.domain.report.PostReport;
import com.nongthinh.post_service.domain.report.valueobject.PostModerationAction;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResolvePostReportUseCaseImpl implements ResolvePostReportUseCase {
    private final PostReportRepository repository;
    private final PostReportUseCaseSupport support;
    private final PostUseCaseSupport postSupport;
    private final PostRepository postRepository;
    private final PostHistoryRecorder historyRecorder;
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
        if (command.moderationAction() == null) {
            throw new BusinessException(ErrorCode.POST_REPORT_MODERATION_ACTION_REQUIRED);
        }

        Post post = postSupport.requirePostForUpdateIncludingDeleted(report.getPostId());
        UUID moderatorId = currentUserProvider.getCurrentUserId();
        Instant now = clockProvider.now();
        String resolutionNote = support.resolutionNote(command.resolutionNote());
        PostModerationAction effectiveAction = moderatePost(
                post, command.moderationAction(), moderatorId, report, resolutionNote, now);

        report.resolve(moderatorId, resolutionNote, now);
        PostReport saved = repository.save(report);
        notificationRecorder.recordResolved(saved, post, moderatorId, effectiveAction, now);
        return PostReportView.from(saved);
    }

    private PostModerationAction moderatePost(Post post, PostModerationAction requestedAction,
                                              UUID moderatorId, PostReport report,
                                              String resolutionNote, Instant now) {
        if (post.isDeleted()) {
            return PostModerationAction.DELETE;
        }

        PostStatus previousStatus = post.getStatus();
        PostVisibility previousVisibility = post.getVisibility();
        PostHistoryAction historyAction;
        if (requestedAction == PostModerationAction.DELETE) {
            post.softDeleteByModerator(now);
            historyAction = PostHistoryAction.DELETED;
        } else {
            if (post.getStatus() == PostStatus.HIDDEN) {
                return PostModerationAction.HIDE;
            }
            if (post.getStatus() != PostStatus.PUBLISHED) {
                throw new BusinessException(ErrorCode.POST_REPORT_POST_MODERATION_CONFLICT);
            }
            post.hide(now);
            historyAction = PostHistoryAction.HIDDEN;
        }

        Post saved = postRepository.save(post);
        String detail = resolutionNote != null ? resolutionNote : report.getReasonDetail();
        historyRecorder.recordModeration(
                saved, moderatorId, historyAction, previousStatus, previousVisibility,
                report.getReason().name(), detail, report.getId(), now);
        return requestedAction;
    }
}
