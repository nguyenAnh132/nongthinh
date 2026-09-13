package com.nongthinh.profile_service.application.service;
import com.nongthinh.profile_service.application.port.out.repository.UserFollowRepository;
import com.nongthinh.profile_service.application.view.FollowProfileView;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import java.util.UUID;
import com.nongthinh.profile_service.common.constant.RoleConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
public class FollowSupport {
    private final CurrentUserProvider users;
    private final UserFollowRepository repository;
    public UUID viewer() {
        var user = users.getCurrentUser();
        if (!user.hasRole(RoleConstant.ROLE_FARMER) && !user.hasRole(RoleConstant.ROLE_BRAND))
            throw new BusinessException(ErrorCode.FORBIDDEN);
        return user.getUserId();
    }
    public FollowProfileView requireProfile(UUID id, UUID viewer) {
        if (id == null) throw new BusinessException(ErrorCode.USER_ID_REQUIRED);
        return repository.profile(id, viewer)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
    }
}
