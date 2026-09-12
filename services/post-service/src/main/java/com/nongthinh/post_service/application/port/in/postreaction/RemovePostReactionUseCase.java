package com.nongthinh.post_service.application.port.in.postreaction;

import com.nongthinh.post_service.application.view.PostEngagementView;

import java.util.UUID;

public interface RemovePostReactionUseCase {
    PostEngagementView execute(UUID postId);
}
