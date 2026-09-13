package com.nongthinh.profile_service.domain.follow;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;

@Getter
public final class UserFollow {
    private final UUID id;
    private final UUID followerUserId;
    private final UUID followedUserId;
    private final Instant createdAt;

    private UserFollow(UUID id, UUID follower, UUID followed, Instant createdAt) {
        if (follower == null || followed == null) throw new BusinessException(ErrorCode.USER_ID_REQUIRED);
        if (follower.equals(followed)) throw new BusinessException(ErrorCode.SELF_FOLLOW_NOT_ALLOWED);
        this.id = Objects.requireNonNull(id);
        this.followerUserId = follower;
        this.followedUserId = followed;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static UserFollow create(UUID id, UUID follower, UUID followed, Instant now) {
        return new UserFollow(id, follower, followed, now);
    }
}
