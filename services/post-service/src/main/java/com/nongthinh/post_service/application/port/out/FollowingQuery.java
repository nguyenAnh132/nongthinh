package com.nongthinh.post_service.application.port.out;

import java.util.Set;
import java.util.UUID;

public interface FollowingQuery {
    Set<UUID> findFollowingUserIds(UUID viewerId);
}
