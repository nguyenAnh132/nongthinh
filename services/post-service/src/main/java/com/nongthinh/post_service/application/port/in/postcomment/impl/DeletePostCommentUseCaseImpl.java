package com.nongthinh.post_service.application.port.in.postcomment.impl;

import com.nongthinh.post_service.application.service.PostEngagementRecorder;
import com.nongthinh.post_service.application.view.DeletedPostCommentView;

import com.nongthinh.post_service.application.port.in.postcomment.DeletePostCommentUseCase;
import com.nongthinh.post_service.application.port.out.ClockProvider;
import com.nongthinh.post_service.application.port.out.repository.CommentRepository;
import com.nongthinh.post_service.application.service.PostCommentUseCaseSupport;
import com.nongthinh.post_service.application.service.PostUseCaseSupport;
import com.nongthinh.post_service.domain.comment.Comment;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeletePostCommentUseCaseImpl implements DeletePostCommentUseCase {
    private final CommentRepository repository;
    private final PostUseCaseSupport postSupport;
    private final PostCommentUseCaseSupport commentSupport;
    private final ClockProvider clockProvider;
    private final PostEngagementRecorder engagement;

    @Override
    @Transactional
    public DeletedPostCommentView execute(UUID postId, UUID commentId) {
        var post = postSupport.requirePostForUpdate(postId);
        Comment comment = commentSupport.requireCommentForUpdate(postId, commentId);
        UUID actorId = commentSupport.currentUserId();
        commentSupport.assertOwner(comment, actorId);
        comment.softDelete(actorId, clockProvider.now());
        repository.save(comment);
        return DeletedPostCommentView.from(commentId,
                engagement.record(post, actorId, false, null, null));
    }
}
