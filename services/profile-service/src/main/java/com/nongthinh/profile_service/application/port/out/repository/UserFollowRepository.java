package com.nongthinh.profile_service.application.port.out.repository;
import com.nongthinh.profile_service.application.view.*;
import com.nongthinh.profile_service.domain.follow.UserFollow;
import java.util.*;
public interface UserFollowRepository {
    Optional<FollowProfileView> profile(UUID userId, UUID viewerId);
    boolean add(UserFollow follow);
    void remove(UUID follower, UUID followed);
    FollowPageView list(UUID userId, UUID viewerId, boolean followers, int page, int size);
    Set<UUID> following(UUID viewerId, List<UUID> userIds);
}
