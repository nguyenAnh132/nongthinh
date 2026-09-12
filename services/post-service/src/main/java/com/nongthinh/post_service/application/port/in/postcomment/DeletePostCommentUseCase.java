package com.nongthinh.post_service.application.port.in.postcomment;

import com.nongthinh.post_service.application.view.DeletedPostCommentView;

import java.util.UUID;

public interface DeletePostCommentUseCase {
    DeletedPostCommentView execute(UUID postId, UUID commentId);
}
