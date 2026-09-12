package com.nongthinh.post_service.application.port.in.post;

import com.nongthinh.post_service.application.command.UpdatePostCommand;
import com.nongthinh.post_service.application.view.PostView;
import java.util.UUID;

public interface UpdatePostUseCase {
    PostView execute(UUID id, UpdatePostCommand command);
}
