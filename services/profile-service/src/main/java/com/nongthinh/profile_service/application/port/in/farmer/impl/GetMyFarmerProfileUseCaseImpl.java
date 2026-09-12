package com.nongthinh.profile_service.application.port.in.farmer.impl;

import org.springframework.stereotype.Service;

import com.nongthinh.profile_service.application.port.in.farmer.GetMyFarmerProfileUseCase;
import com.nongthinh.profile_service.application.port.out.LocationServicePort;
import com.nongthinh.profile_service.application.port.out.repository.FarmerProfileRepository;
import com.nongthinh.profile_service.application.view.FarmerProfileView;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import com.nongthinh.profile_service.domain.farmerprofile.FarmerProfile;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMyFarmerProfileUseCaseImpl implements GetMyFarmerProfileUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final FarmerProfileRepository farmerProfileRepository;
    private final LocationServicePort locationServicePort;

    @Override
    public FarmerProfileView execute() {
        var userId = currentUserProvider.getCurrentUser().getUserId();
        FarmerProfile profile = farmerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        var address = profile.getAddress();
        var addressNames = locationServicePort.resolveAddress(
                address.getProvinceId(),
                address.getCommuneId()
        );
        return FarmerProfileView.from(profile, addressNames);
    }
}
