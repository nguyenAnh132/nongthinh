package com.nongthinh.profile_service.application.port.in.brand.impl;

import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.command.brand.UpdateMyBrandOfficeAddressCommand;
import com.nongthinh.profile_service.application.port.in.brand.UpdateMyBrandOfficeAddressUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.LocationServicePort;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.application.view.BrandProfileView;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import com.nongthinh.profile_service.domain.shared.valueobject.Address;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateMyBrandOfficeAddressUseCaseImpl implements UpdateMyBrandOfficeAddressUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final BrandProfileRepository brandProfileRepository;
    private final ClockProvider clockProvider;
    private final LocationServicePort locationServicePort;

    @Override
    public BrandProfileView execute(UpdateMyBrandOfficeAddressCommand command) {
        var userId = currentUserProvider.getCurrentUser().getUserId();
        BrandProfile profile = brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        if (command.provinceId() != null || command.communeId() != null) {
            locationServicePort.validateAddress(command.provinceId(), command.communeId());
        }
        profile.updateOfficeAddress(
                Address.of(command.provinceId(), command.communeId(), command.addressDetail()),
                clockProvider.now()
        );
        return BrandProfileView.from(brandProfileRepository.save(profile));
    }
}
