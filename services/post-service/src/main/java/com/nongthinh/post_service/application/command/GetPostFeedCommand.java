package com.nongthinh.post_service.application.command;

import java.util.UUID;

public record GetPostFeedCommand(
        String cursor, UUID postTypeId, UUID topicId, UUID cropTypeId,
        UUID authorUserId, String keyword, boolean followingOnly
) { }
