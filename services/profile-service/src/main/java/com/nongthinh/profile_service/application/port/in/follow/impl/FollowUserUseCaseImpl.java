package com.nongthinh.profile_service.application.port.in.follow.impl;
import com.nongthinh.profile_service.application.port.in.follow.FollowUserUseCase;
import com.nongthinh.profile_service.application.port.out.*;
import com.nongthinh.profile_service.application.port.out.repository.UserFollowRepository;
import com.nongthinh.profile_service.application.service.FollowSupport;
import com.nongthinh.profile_service.application.view.FollowProfileView;
import com.nongthinh.profile_service.domain.follow.UserFollow;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class FollowUserUseCaseImpl implements FollowUserUseCase {
    private final FollowSupport support;
    private final UserFollowRepository repository;
    private final FollowNotificationRepository notifications;
    private final IdGenerator ids;
    private final ClockProvider clock;
    @Transactional
    public FollowProfileView execute(UUID userId) {
        UUID viewer = support.viewer();
        var follow = UserFollow.create(ids.generate(), viewer, userId, clock.now());
        support.requireProfile(viewer, viewer);
        support.requireProfile(userId, viewer);
        if (repository.add(follow))
            notifications.newFollower(follow.getId(), viewer, userId, follow.getCreatedAt());
        return support.requireProfile(userId, viewer);
    }
}
