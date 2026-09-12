package com.nongthinh.profile_service.application.port.in.farmer.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.farmer.UpdateMyFarmerAddressUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.LocationServicePort;
import com.nongthinh.profile_service.application.port.out.repository.FarmerProfileRepository;
import com.nongthinh.profile_service.application.view.FarmerProfileView;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import com.nongthinh.profile_service.domain.farmerprofile.FarmerProfile;
import com.nongthinh.profile_service.domain.shared.valueobject.Address;
import lombok.RequiredArgsConstructor;
import com.nongthinh.profile_service.application.command.farmer.UpdateMyFarmerAddressCommand;

@Service
@RequiredArgsConstructor
public class UpdateMyFarmerAddressUseCaseImpl implements UpdateMyFarmerAddressUseCase {

    private final FarmerProfileRepository farmerProfileRepository;
    private final ClockProvider clockProvider;
    private final LocationServicePort locationServicePort;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public FarmerProfileView execute(UpdateMyFarmerAddressCommand command) {
        UUID userId = currentUserProvider.getCurrentUser().getUserId();
        FarmerProfile profile = farmerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        if (command.provinceId() != null || command.communeId() != null) {
            locationServicePort.validateAddress(command.provinceId(), command.communeId());
        }
        profile.updateAddress(Address.of(command.provinceId(), command.communeId(), command.addressDetail()), clockProvider.now());
        return FarmerProfileView.from(farmerProfileRepository.save(profile));
    }
}
