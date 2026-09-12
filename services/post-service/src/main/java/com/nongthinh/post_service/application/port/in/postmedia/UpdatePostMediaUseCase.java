package com.nongthinh.post_service.application.port.in.postmedia;

import com.nongthinh.post_service.application.command.UpdatePostMediaCommand;
import com.nongthinh.post_service.application.view.PostMediaView;
import java.util.UUID;

public interface UpdatePostMediaUseCase {
    PostMediaView execute(UUID postId, UUID mediaId, UpdatePostMediaCommand command);
}
