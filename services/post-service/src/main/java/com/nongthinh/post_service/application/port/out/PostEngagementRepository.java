package com.nongthinh.post_service.application.port.out;

import com.nongthinh.post_service.application.event.PostEngagementEvent;
import com.nongthinh.post_service.application.view.PostEngagementView;
import java.util.UUID;

public interface PostEngagementRepository {
    PostEngagementView snapshot(UUID postId, UUID actorId);
    void incrementVersion(UUID postId, boolean reaction);
    void append(PostEngagementEvent event);
}
