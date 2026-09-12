package com.nongthinh.post_service.application.port.in.postcomment.impl;

import com.nongthinh.post_service.application.service.PostEngagementRecorder;

import com.nongthinh.post_service.application.command.UpdatePostCommentCommand;
import com.nongthinh.post_service.application.port.in.postcomment.UpdatePostCommentUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.repository.CommentRepository;
import com.nongthinh.post_service.application.service.PostCommentUseCaseSupport;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PostCommentView;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.comment.Comment;
import com.nongthinh.post_service.domain.exception.BusinessException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdatePostCommentUseCaseImpl implements UpdatePostCommentUseCase {
    private final CommentRepository repository;
    private final PostUseCaseSupport postSupport;
    private final PostCommentUseCaseSupport commentSupport;
    private final ClockProvider clockProvider;
    private final PostEngagementRecorder engagement;

    @Override
    @Transactional
    public PostCommentView execute(UUID postId, UUID commentId, UpdatePostCommentCommand command) {
        if (command == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }
        var post = postSupport.requirePostForUpdate(postId);
        Comment comment = commentSupport.requireCommentForUpdate(postId, commentId);
        UUID actorId = commentSupport.currentUserId();
        commentSupport.assertOwner(comment, actorId);
        commentSupport.assertEditable(comment);
        comment.edit(actorId, commentSupport.content(command.content()), clockProvider.now());
        var saved = repository.save(comment);
        return PostCommentView.from(saved).withMetrics(engagement.record(post, actorId, false, null, null));
    }
}
