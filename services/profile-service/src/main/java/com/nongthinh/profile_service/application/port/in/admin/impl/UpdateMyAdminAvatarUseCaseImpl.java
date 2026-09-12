package com.nongthinh.profile_service.application.port.in.admin.impl;

import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.admin.UpdateMyAdminAvatarUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.repository.AdminProfileRepository;
import com.nongthinh.profile_service.application.view.AdminProfileView;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.adminprofile.AdminProfile;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateMyAdminAvatarUseCaseImpl implements UpdateMyAdminAvatarUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final AdminProfileRepository adminProfileRepository;
    private final ClockProvider clockProvider;

    @Override
    public AdminProfileView execute(String avatarUrl) {
        var userId = currentUserProvider.getCurrentUser().getUserId();
        AdminProfile profile = adminProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        profile.updateAvatarUrl(avatarUrl, clockProvider.now());
        return AdminProfileView.from(adminProfileRepository.save(profile));
    }
}
