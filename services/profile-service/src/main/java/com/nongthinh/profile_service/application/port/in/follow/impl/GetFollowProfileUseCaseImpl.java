package com.nongthinh.profile_service.application.port.in.follow.impl;
import com.nongthinh.profile_service.application.port.in.follow.GetFollowProfileUseCase;
import com.nongthinh.profile_service.application.service.FollowSupport;
import com.nongthinh.profile_service.application.view.FollowProfileView;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class GetFollowProfileUseCaseImpl implements GetFollowProfileUseCase {
    private final FollowSupport support;
    public FollowProfileView execute(UUID userId) { return support.requireProfile(userId, support.viewer()); }
}
