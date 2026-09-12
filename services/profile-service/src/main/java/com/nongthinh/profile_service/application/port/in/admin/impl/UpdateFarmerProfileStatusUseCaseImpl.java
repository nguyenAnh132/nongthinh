package com.nongthinh.profile_service.application.port.in.admin.impl;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.admin.UpdateFarmerProfileStatusUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.repository.FarmerProfileRepository;
import com.nongthinh.profile_service.application.view.FarmerProfileView;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import com.nongthinh.profile_service.domain.farmerprofile.FarmerProfile;
import com.nongthinh.profile_service.domain.shared.valueobject.StandardProfileStatus;
import com.nongthinh.profile_service.presentation.dto.request.admin.ProfileStatusUpdateRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateFarmerProfileStatusUseCaseImpl implements UpdateFarmerProfileStatusUseCase {

    private final FarmerProfileRepository farmerProfileRepository;
    private final ClockProvider clockProvider;

    @Override
    public FarmerProfileView execute(UUID id, ProfileStatusUpdateRequest request) {
        FarmerProfile profile = farmerProfileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        Instant now = clockProvider.now();
        applyStatus(profile, StandardProfileStatus.fromString(request.status()), now);
        return FarmerProfileView.from(farmerProfileRepository.save(profile));
    }

    private void applyStatus(FarmerProfile profile, StandardProfileStatus status, Instant now) {
        switch (status) {
            case ACTIVE -> profile.activate(now);
            case LOCKED -> profile.lock(now);
            case DISABLED -> profile.disable(now);
            case DELETED -> profile.markDeleted(now);
        }
    }
}
