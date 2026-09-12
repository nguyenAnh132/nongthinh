package com.nongthinh.post_service.application.port.in.postmedia;

import com.nongthinh.post_service.application.command.CreatePostMediaCommand;
import com.nongthinh.post_service.application.view.PostMediaView;
import java.util.UUID;

public interface CreatePostMediaUseCase {
    PostMediaView execute(UUID postId, CreatePostMediaCommand command);
}
