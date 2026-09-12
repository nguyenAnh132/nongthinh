package com.nongthinh.profile_service.application.port.in.admin.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.admin.GetPublicAdminProfileByUserIdUseCase;
import com.nongthinh.profile_service.application.port.out.repository.AdminProfileRepository;
import com.nongthinh.profile_service.application.view.AdminProfilePublicView;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.adminprofile.AdminProfile;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetPublicAdminProfileByUserIdUseCaseImpl implements GetPublicAdminProfileByUserIdUseCase {

    private final AdminProfileRepository adminProfileRepository;

    @Override
    public AdminProfilePublicView execute(UUID userId) {
        AdminProfile profile = adminProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        profile.ensureActive();
        return AdminProfilePublicView.from(profile);
    }
}
