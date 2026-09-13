package com.nongthinh.profile_service.application.view;
import java.util.UUID;
public record FollowProfileView(UUID userId, String displayName, String avatarUrl, String role,
        long followerCount, long followingCount, boolean following) {}
