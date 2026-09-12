package com.nongthinh.profile_service.application.port.in.brand.impl;

import java.time.Instant;
import org.springframework.stereotype.Service;
import com.nongthinh.profile_service.application.port.in.brand.UpdateMyBrandProfileUseCase;
import com.nongthinh.profile_service.application.port.out.ClockProvider;
import com.nongthinh.profile_service.application.port.out.LocationServicePort;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.application.view.BrandProfileView;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.common.exception.ErrorCode;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import com.nongthinh.profile_service.domain.brandprofile.valueobject.BrandName;
import com.nongthinh.profile_service.domain.exception.BusinessException;
import com.nongthinh.profile_service.domain.shared.valueobject.Address;
import com.nongthinh.profile_service.presentation.dto.request.me.MyBrandProfileUpdateRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateMyBrandProfileUseCaseImpl implements UpdateMyBrandProfileUseCase {

    private final CurrentUserProvider currentUserProvider;
    private final BrandProfileRepository brandProfileRepository;
    private final ClockProvider clockProvider;
    private final LocationServicePort locationServicePort;

    @Override
    public BrandProfileView execute(MyBrandProfileUpdateRequest request) {
        var userId = currentUserProvider.getCurrentUser().getUserId();
        BrandProfile profile = brandProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        Instant now = clockProvider.now();

        profile.updateBrandName(BrandName.of(request.brandName()), now);
        profile.updateTaxCode(request.taxCode(), now);
        profile.updateDescription(request.description(), now);
        profile.updatePhone(request.phone(), now);
        profile.updateRepresentativeName(request.representativeName(), now);
        profile.updateRepresentativePhone(request.representativePhone(), now);
        profile.updateRepresentativeEmail(request.representativeEmail(), now);
        if (request.officeProvinceId() != null || request.officeCommuneId() != null) {
            locationServicePort.validateAddress(request.officeProvinceId(), request.officeCommuneId());
        }
        profile.updateOfficeAddress(
                Address.of(request.officeProvinceId(), request.officeCommuneId(), request.officeAddressDetail()),
                now
        );
        if (request.logoUrl() != null) {
            profile.updateLogoUrl(request.logoUrl(), now);
        }
        if (request.bannerUrl() != null) {
            profile.updateBannerUrl(request.bannerUrl(), now);
        }
        if (request.websiteUrl() != null) {
            profile.updateWebsiteUrl(request.websiteUrl(), now);
        }

        return BrandProfileView.from(brandProfileRepository.save(profile));
    }
}
