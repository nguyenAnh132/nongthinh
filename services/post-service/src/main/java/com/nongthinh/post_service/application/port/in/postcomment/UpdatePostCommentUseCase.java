package com.nongthinh.post_service.application.port.in.postcomment;

import com.nongthinh.post_service.application.command.UpdatePostCommentCommand;
import com.nongthinh.post_service.application.view.PostCommentView;
import java.util.UUID;

public interface UpdatePostCommentUseCase {
    PostCommentView execute(UUID postId, UUID commentId, UpdatePostCommentCommand command);
}
