package com.nongthinh.profile_service.application.port.in.follow.impl;
import com.nongthinh.profile_service.application.port.in.follow.UnfollowUserUseCase;
import com.nongthinh.profile_service.application.port.out.repository.UserFollowRepository;
import com.nongthinh.profile_service.application.service.FollowSupport;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class UnfollowUserUseCaseImpl implements UnfollowUserUseCase {
    private final FollowSupport support;
    private final UserFollowRepository repository;
    @Transactional
    public void execute(UUID userId) {
        // Allow removing a relation even if the target profile has since been disabled.
        repository.remove(support.viewer(), userId);
    }
}
