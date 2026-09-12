package com.nongthinh.post_service.application.port.in.post;

import com.nongthinh.post_service.application.command.CreatePostCommand;
import com.nongthinh.post_service.application.view.PostView;

public interface CreatePostUseCase {
    PostView execute(CreatePostCommand command);
}
