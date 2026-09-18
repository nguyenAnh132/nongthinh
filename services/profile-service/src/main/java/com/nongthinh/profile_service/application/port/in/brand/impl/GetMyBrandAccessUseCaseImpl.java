package com.nongthinh.profile_service.application.port.in.brand.impl;

import com.nongthinh.profile_service.application.port.in.brand.GetMyBrandAccessUseCase;
import com.nongthinh.profile_service.application.port.out.repository.BrandProfileRepository;
import com.nongthinh.profile_service.application.view.BrandAccessView;
import com.nongthinh.profile_service.common.currentuser.CurrentUserProvider;
import com.nongthinh.profile_service.domain.brandprofile.BrandAccessPolicy;
import com.nongthinh.profile_service.domain.brandprofile.BrandProfile;
import com.nongthinh.profile_service.domain.brandprofile.valueobject.BrandProfileStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetMyBrandAccessUseCaseImpl implements GetMyBrandAccessUseCase {
    private final CurrentUserProvider currentUserProvider;
    private final BrandProfileRepository brandProfileRepository;

    @Override
    public BrandAccessView execute() {
        var status = brandProfileRepository.findByUserId(currentUserProvider.getCurrentUser().getUserId())
                .map(BrandProfile::getStatus).orElse(null);
        return new BrandAccessView(status == BrandProfileStatus.ACTIVE,
                BrandAccessPolicy.canEditProfile(status), BrandAccessPolicy.canSubmitDocuments(status));
    }
}
