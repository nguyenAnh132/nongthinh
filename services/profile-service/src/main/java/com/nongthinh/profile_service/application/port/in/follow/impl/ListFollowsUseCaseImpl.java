package com.nongthinh.profile_service.application.port.in.follow.impl;
import com.nongthinh.profile_service.application.port.in.follow.ListFollowsUseCase;
import com.nongthinh.profile_service.application.port.out.repository.UserFollowRepository;
import com.nongthinh.profile_service.application.service.FollowSupport;
import com.nongthinh.profile_service.application.view.FollowPageView;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class ListFollowsUseCaseImpl implements ListFollowsUseCase {
    private final FollowSupport support;
    private final UserFollowRepository repository;
    public FollowPageView execute(UUID userId, boolean followers, int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        var viewer = support.viewer();
        support.requireProfile(userId, viewer);
        return repository.list(userId, viewer, followers, page, size);
    }
}
