package com.nongthinh.post_service.application.view;

import com.nongthinh.post_service.domain.interaction.PostShare;
import java.time.Instant;
import java.util.UUID;

public record PostShareView(
        UUID id,
        UUID postId,
        UUID sharedByUserId,
        Instant createdAt
) {
    public static PostShareView from(PostShare share) {
        return new PostShareView(
                share.id(), share.postId(), share.sharedByUserId(), share.createdAt());
    }
}
