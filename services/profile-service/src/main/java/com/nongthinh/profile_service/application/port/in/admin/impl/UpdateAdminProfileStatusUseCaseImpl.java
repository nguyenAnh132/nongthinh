package com.nongthinh.profile_service.application.port.in.admin.impl;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.admin.UpdateAdminProfileStatusUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.repository.AdminProfileRepository;
import com.nongthinh.profile_service.application.view.AdminProfileView;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.adminprofile.AdminProfile;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import com.nongthinh.profile_service.domain.shared.valueobject.StandardProfileStatus;
import com.nongthinh.profile_service.presentation.dto.request.admin.ProfileStatusUpdateRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateAdminProfileStatusUseCaseImpl implements UpdateAdminProfileStatusUseCase {

    private final AdminProfileRepository adminProfileRepository;
    private final ClockProvider clockProvider;

    @Override
    public AdminProfileView execute(UUID id, ProfileStatusUpdateRequest request) {
        AdminProfile profile = adminProfileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        Instant now = clockProvider.now();
        applyStatus(profile, StandardProfileStatus.fromString(request.status()), now);
        return AdminProfileView.from(adminProfileRepository.save(profile));
    }

    private void applyStatus(AdminProfile profile, StandardProfileStatus status, Instant now) {
        switch (status) {
            case ACTIVE -> profile.activate(now);
            case LOCKED -> profile.lock(now);
            case DISABLED -> profile.disable(now);
            case DELETED -> profile.markDeleted(now);
        }
    }
}
