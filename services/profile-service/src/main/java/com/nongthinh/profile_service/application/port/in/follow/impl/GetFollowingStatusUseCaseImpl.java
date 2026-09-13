package com.nongthinh.profile_service.application.port.in.follow.impl;
import com.nongthinh.profile_service.application.port.in.follow.GetFollowingStatusUseCase;
import com.nongthinh.profile_service.application.port.out.repository.UserFollowRepository;
import com.nongthinh.profile_service.application.service.FollowSupport;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class GetFollowingStatusUseCaseImpl implements GetFollowingStatusUseCase {
    private final FollowSupport support;
    private final UserFollowRepository repository;
    public Set<UUID> execute(List<UUID> userIds) {
        if (userIds == null || userIds.size() > 100 || userIds.stream().anyMatch(Objects::isNull))
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        return repository.following(support.viewer(), userIds);
    }
}
