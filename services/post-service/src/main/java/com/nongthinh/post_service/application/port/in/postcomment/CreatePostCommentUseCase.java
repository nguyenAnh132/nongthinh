package com.nongthinh.post_service.application.port.in.postcomment;

import com.nongthinh.post_service.application.command.CreatePostCommentCommand;
import com.nongthinh.post_service.application.view.PostCommentView;
import java.util.UUID;

public interface CreatePostCommentUseCase {
    PostCommentView execute(UUID postId, CreatePostCommentCommand command);
}
