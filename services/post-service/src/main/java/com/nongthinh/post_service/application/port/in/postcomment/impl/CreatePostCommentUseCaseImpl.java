package com.nongthinh.post_service.application.port.in.postcomment.impl;

import com.nongthinh.post_service.application.service.PostEngagementRecorder;

import com.nongthinh.post_service.application.command.CreatePostCommentCommand;
import com.nongthinh.post_service.application.port.in.postcomment.CreatePostCommentUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.IdGenerator;
import com.nongthinh.post_service.application.port.out.repository.CommentRepository;
import com.nongthinh.post_service.application.service.PostCommentUseCaseSupport;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.application.view.PostCommentView;
import com.nongthinh.post_service.common.exception.ErrorCode;
import com.nongthinh.post_service.domain.comment.Comment;
import com.nongthinh.post_service.domain.exception.BusinessException;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreatePostCommentUseCaseImpl implements CreatePostCommentUseCase {
    private final CommentRepository repository;
    private final PostUseCaseSupport postSupport;
    private final PostCommentUseCaseSupport commentSupport;
    private final IdGenerator idGenerator;
    private final ClockProvider clockProvider;
    private final PostEngagementRecorder engagement;

    @Override
    @Transactional
    public PostCommentView execute(UUID postId, CreatePostCommentCommand command) {
        if (command == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST_PARAMETER);
        }
        var post = postSupport.requirePostForUpdate(postId);
        postSupport.requireInteractablePost(postId);
        UUID recipientId = post.getAuthorUserId().value();
        UUID authorId = commentSupport.currentUserId();
        Instant now = clockProvider.now();
        String content = commentSupport.content(command.content());
        Comment comment;
        if (command.parentCommentId() == null) {
            comment = Comment.createRoot(idGenerator.generate(), postId, authorId, content, now);
        } else {
            Comment parent = commentSupport.requireReplyParent(postId, command.parentCommentId());
            recipientId = parent.getAuthorUserId();
            comment = Comment.createReply(
                    idGenerator.generate(), parent, authorId, content, now);
        }
        var saved = repository.save(comment);
        var metrics = engagement.record(post, authorId, false, recipientId,
                command.parentCommentId() == null ? "POST_COMMENT" : "COMMENT_REPLY");
        return PostCommentView.from(saved).withMetrics(metrics);
    }
}
