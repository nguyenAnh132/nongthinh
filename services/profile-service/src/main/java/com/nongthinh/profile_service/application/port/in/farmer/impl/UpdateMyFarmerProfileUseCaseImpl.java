package com.nongthinh.profile_service.application.port.in.farmer.impl;

import java.time.Instant;
import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.farmer.UpdateMyFarmerProfileUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.LocationServicePort;
import com.nongthinh.profile_service.application.port.out.repository.FarmerProfileRepository;
import com.nongthinh.profile_service.application.view.FarmerProfileView;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import com.nongthinh.profile_service.domain.farmerprofile.FarmerProfile;
import com.nongthinh.profile_service.domain.farmerprofile.valueobject.Gender;
import com.nongthinh.profile_service.domain.shared.valueobject.Address;
import com.nongthinh.profile_service.domain.shared.valueobject.PersonName;
import com.nongthinh.profile_service.presentation.dto.request.me.MyFarmerProfileUpdateRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateMyFarmerProfileUseCaseImpl implements UpdateMyFarmerProfileUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final FarmerProfileRepository farmerProfileRepository;
    private final ClockProvider clockProvider;
    private final LocationServicePort locationServicePort;

    @Override
    public FarmerProfileView execute(MyFarmerProfileUpdateRequest request) {
        var userId = currentUserProvider.getCurrentUser().getUserId();
        FarmerProfile profile = farmerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        Instant now = clockProvider.now();

        profile.updateFirstName(PersonName.of(request.firstName()), now);
        profile.updateLastName(PersonName.of(request.lastName()), now);
        profile.updateGender(Gender.fromString(request.gender()), now);
        if (request.phone() != null) {
            profile.updatePhone(request.phone(), now);
        }
        if (request.provinceId() != null || request.communeId() != null) {
            locationServicePort.validateAddress(request.provinceId(), request.communeId());
        }
        profile.updateAddress(
                Address.of(request.provinceId(), request.communeId(), request.addressDetail()),
                now
        );
        if (request.avatarUrl() != null) {
            profile.updateAvatarUrl(request.avatarUrl(), now);
        }

        FarmerProfile savedProfile = farmerProfileRepository.save(profile);
        var addressNames = locationServicePort.resolveAddress(
                savedProfile.getAddress().getProvinceId(),
                savedProfile.getAddress().getCommuneId()
        );
        return FarmerProfileView.from(savedProfile, addressNames);
    }
}
