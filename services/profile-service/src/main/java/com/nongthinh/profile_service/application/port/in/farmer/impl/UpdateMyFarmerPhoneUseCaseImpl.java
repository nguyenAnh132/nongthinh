package com.nongthinh.profile_service.application.port.in.farmer.impl;

import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.farmer.UpdateMyFarmerPhoneUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.repository.FarmerProfileRepository;
import com.nongthinh.profile_service.application.view.FarmerProfileView;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import com.nongthinh.profile_service.domain.farmerprofile.FarmerProfile;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateMyFarmerPhoneUseCaseImpl implements UpdateMyFarmerPhoneUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final FarmerProfileRepository farmerProfileRepository;
    private final ClockProvider clockProvider;

    @Override
    public FarmerProfileView execute(String phone) {
        var userId = currentUserProvider.getCurrentUser().getUserId();
        FarmerProfile profile = farmerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        profile.updatePhone(phone, clockProvider.now());
        return FarmerProfileView.from(farmerProfileRepository.save(profile));
    }
}
