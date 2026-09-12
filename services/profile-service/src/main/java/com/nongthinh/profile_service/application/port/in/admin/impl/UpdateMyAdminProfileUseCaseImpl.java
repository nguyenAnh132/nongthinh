package com.nongthinh.profile_service.application.port.in.admin.impl;

import java.time.Instant;
import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.admin.UpdateMyAdminProfileUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.repository.AdminProfileRepository;
import com.nongthinh.profile_service.application.view.AdminProfileView;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.adminprofile.AdminProfile;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import com.nongthinh.profile_service.domain.shared.valueobject.PersonName;
import com.nongthinh.profile_service.presentation.dto.request.me.MyAdminProfileUpdateRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateMyAdminProfileUseCaseImpl implements UpdateMyAdminProfileUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final AdminProfileRepository adminProfileRepository;
    private final ClockProvider clockProvider;

    @Override
    public AdminProfileView execute(MyAdminProfileUpdateRequest request) {
        var userId = currentUserProvider.getCurrentUser().getUserId();
        AdminProfile profile = adminProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        Instant now = clockProvider.now();

        profile.updateFirstName(PersonName.of(request.firstName()), now);
        profile.updateLastName(PersonName.of(request.lastName()), now);
        if (request.phone() != null) {
            profile.updatePhone(request.phone(), now);
        }
        if (request.avatarUrl() != null) {
            profile.updateAvatarUrl(request.avatarUrl(), now);
        }

        return AdminProfileView.from(adminProfileRepository.save(profile));
    }
}
