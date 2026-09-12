package com.nongthinh.post_service.application.port.in.postreaction;

import com.nongthinh.post_service.application.command.SetPostReactionCommand;
import com.nongthinh.post_service.application.view.PostReactionView;
import java.util.UUID;

public interface SetPostReactionUseCase {
    PostReactionView execute(UUID postId, SetPostReactionCommand command);
}
